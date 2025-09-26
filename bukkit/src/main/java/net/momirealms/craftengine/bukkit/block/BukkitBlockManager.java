package net.momirealms.craftengine.bukkit.block;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.injector.BlockGenerator;
import net.momirealms.craftengine.bukkit.plugin.reflection.bukkit.CraftBukkitReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MBlocks;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MBuiltInRegistries;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MRegistries;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.bukkit.util.RegistryUtils;
import net.momirealms.craftengine.bukkit.util.TagUtils;
import net.momirealms.craftengine.core.block.*;
import net.momirealms.craftengine.core.block.behavior.EmptyBlockBehavior;
import net.momirealms.craftengine.core.block.parser.BlockStateParser;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.plugin.logger.Debugger;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Holder;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.sound.SoundData;
import net.momirealms.craftengine.core.util.*;
import net.momirealms.craftengine.core.world.chunk.PalettedContainer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class BukkitBlockManager extends AbstractBlockManager {
    public static final Set<Object> CLIENT_SIDE_NOTE_BLOCKS = new HashSet<>(2048, 0.6f);
    private static final Object ALWAYS_FALSE = FastNMS.INSTANCE.method$StatePredicate$always(false);
    private static final Object ALWAYS_TRUE = FastNMS.INSTANCE.method$StatePredicate$always(true);
    private static BukkitBlockManager instance;
    private final BukkitCraftEngine plugin;
    // 事件监听器
    private BlockEventListener blockEventListener;
    // 可燃烧的方块
    private Map<Object, Integer> igniteOdds;
    private Map<Object, Integer> burnOdds;
    // 自定义客户端侧原版方块标签
    private Map<Integer, List<String>> clientBoundTags = Map.of();
    private Map<Integer, List<String>> previousClientBoundTags = Map.of();
    // 缓存的原版方块tag包
    private Object cachedUpdateTagsPacket;
    // 被移除声音的原版方块
    private final Set<Object> replacedBlockSounds = new HashSet<>();
    // 用于缓存string形式的方块状态到原版方块状态
    private final Map<String, Object> blockStateCache = new HashMap<>(1024);

    public BukkitBlockManager(BukkitCraftEngine plugin) {
        super(plugin, RegistryUtils.currentBlockRegistrySize(), Config.serverSideBlocks());
        this.plugin = plugin;
        this.registerServerSideCustomBlocks(Config.serverSideBlocks());
        this.registerEmptyBlock();
        instance = this;
    }

    @Override
    public void init() {
        this.initMirrorRegistry();
        this.initFireBlock();
        this.initVanillaBlockSettings();
        this.deceiveBukkitRegistry();
        this.markVanillaNoteBlocks();
        this.blockEventListener = new BlockEventListener(plugin, this);
        Arrays.fill(this.immutableBlockStates, EmptyBlock.INSTANCE.defaultState());
        this.plugin.networkManager().registerBlockStatePacketListeners(this.blockStateMappings); // 一定要预先初始化一次，预防id超出上限
    }

    public static BukkitBlockManager instance() {
        return instance;
    }

    @Override
    public void delayedInit() {
        Bukkit.getPluginManager().registerEvents(this.blockEventListener, this.plugin.javaPlugin());
    }

    @Override
    public void unload() {
        super.unload();
        Arrays.fill(this.blockStateMappings, -1);
        this.previousClientBoundTags = this.clientBoundTags;
        this.clientBoundTags = new HashMap<>();
        if (EmptyBlock.STATE != null)
            Arrays.fill(this.immutableBlockStates, EmptyBlock.STATE);
        for (DelegatingBlock block : this.customBlocks) {
            block.behaviorDelegate().bindValue(EmptyBlockBehavior.INSTANCE);
            block.shapeDelegate().bindValue(BukkitBlockShape.STONE);
            DelegatingBlockState state = (DelegatingBlockState) FastNMS.INSTANCE.method$Block$defaultState(block);
            state.setBlockState(null);
        }
    }

    @Override
    public void disable() {
        this.unload();
        HandlerList.unregisterAll(this.blockEventListener);
    }

    @Override
    public void delayedLoad() {
        this.plugin.networkManager().registerBlockStatePacketListeners(this.blockStateMappings); // 重置方块映射表
        super.delayedLoad();
    }

    @Override
    protected void resendTags() {
        // if there's no change
        if (this.clientBoundTags.equals(this.previousClientBoundTags)) return;
        List<TagUtils.TagEntry> list = new ArrayList<>();
        for (Map.Entry<Integer, List<String>> entry : this.clientBoundTags.entrySet()) {
            list.add(new TagUtils.TagEntry(entry.getKey(), entry.getValue()));
        }
        Object packet = TagUtils.createUpdateTagsPacket(Map.of(MRegistries.BLOCK, list));
        for (BukkitServerPlayer player : this.plugin.networkManager().onlineUsers()) {
            player.sendPacket(packet, false);
        }
        // 如果空，那么新来的玩家就没必要收到更新包了
        if (list.isEmpty()) {
            this.cachedUpdateTagsPacket = null;
        } else {
            this.cachedUpdateTagsPacket = packet;
        }
    }

    @Nullable
    @Override
    public BlockStateWrapper createBlockState(String blockState) {
        ImmutableBlockState state = BlockStateParser.deserialize(blockState);
        if (state != null) {
            return state.customBlockState();
        }
        return createVanillaBlockState(blockState);
    }

    @Override
    public BlockStateWrapper createVanillaBlockState(String blockState) {
        Object state = parseBlockState(blockState);
        if (state == null) return null;
        return BlockStateUtils.toBlockStateWrapper(state);
    }

    @Nullable
    private Object parseBlockState(String state) {
        if (this.blockStateCache.containsKey(state)) {
            return this.blockStateCache.get(state);
        }
        try {
            Object registryOrLookUp = MBuiltInRegistries.BLOCK;
            if (CoreReflections.method$Registry$asLookup != null) {
                registryOrLookUp = CoreReflections.method$Registry$asLookup.invoke(registryOrLookUp);
            }
            Object result = CoreReflections.method$BlockStateParser$parseForBlock.invoke(null, registryOrLookUp, state, false);
            Object resultState = CoreReflections.method$BlockStateParser$BlockResult$blockState.invoke(result);
            this.blockStateCache.put(state, resultState);
            return resultState;
        } catch (Exception e) {
            Debugger.BLOCK.warn(() -> "Failed to create block state: " + state, e);
            return null;
        }
    }

    @Nullable
    public Object getMinecraftBlockHolder(int stateId) {
        return this.customBlockHolders[stateId - BlockStateUtils.vanillaBlockStateCount()];
    }

    @Override
    public Key getBlockOwnerId(BlockStateWrapper state) {
        return BlockStateUtils.getBlockOwnerIdFromState(state.literalObject());
    }

    @Override
    protected Key getBlockOwnerId(int id) {
        return BlockStateUtils.getBlockOwnerIdFromState(BlockStateUtils.idToBlockState(id));
    }

    @SuppressWarnings("unchecked")
    private void initFireBlock() {
        try {
            this.igniteOdds = (Map<Object, Integer>) CoreReflections.field$FireBlock$igniteOdds.get(MBlocks.FIRE);
            this.burnOdds = (Map<Object, Integer>) CoreReflections.field$FireBlock$burnOdds.get(MBlocks.FIRE);
        } catch (IllegalAccessException e) {
            this.plugin.logger().warn("Failed to get ignite odds", e);
        }
    }

    private void initVanillaBlockSettings() {
        try {
            for (int i = 0; i < this.vanillaBlockStateCount; i++) {
                Object blockState = BlockStateUtils.idToBlockState(i);
                // 确保缓存已被激活
                CoreReflections.method$BlockStateBase$initCache.invoke(blockState);
                BlockSettings settings = BlockSettings.of()
                        .pushReaction(PushReaction.VALUES[((Enum<?>) CoreReflections.field$BlockStateBase$pushReaction.get(blockState)).ordinal()])
                        .mapColor(MapColor.get(CoreReflections.field$MapColor$id.getInt(CoreReflections.field$BlockStateBase$mapColor.get(blockState))))
                        .canOcclude(FastNMS.INSTANCE.method$BlockStateBase$canOcclude(blockState) ? Tristate.TRUE : Tristate.FALSE)
                        .isRandomlyTicking(CoreReflections.field$BlockStateBase$isRandomlyTicking.getBoolean(blockState))
                        .hardness(CoreReflections.field$BlockStateBase$hardness.getFloat(blockState))
                        .replaceable(CoreReflections.field$BlockStateBase$replaceable.getBoolean(blockState))
                        .burnable(CoreReflections.field$BlockStateBase$burnable.getBoolean(blockState))
                        .luminance(CoreReflections.field$BlockStateBase$lightEmission.getInt(blockState))
                        .instrument(Instrument.VALUES[((Enum<?>) CoreReflections.field$BlockStateBase$instrument.get(blockState)).ordinal()])
                        .pushReaction(PushReaction.VALUES[((Enum<?>) CoreReflections.field$BlockStateBase$pushReaction.get(blockState)).ordinal()]);
                Object block = BlockStateUtils.getBlockOwner(blockState);
                settings.resistance(CoreReflections.field$BlockBehaviour$explosionResistance.getFloat(block))
                        .friction(CoreReflections.field$BlockBehaviour$friction.getFloat(block))
                        .speedFactor(CoreReflections.field$BlockBehaviour$speedFactor.getFloat(block))
                        .jumpFactor(CoreReflections.field$BlockBehaviour$jumpFactor.getFloat(block))
                        .sounds(toBlockSounds(CoreReflections.field$BlockBehaviour$soundType.get(block)));
                if (VersionHelper.isOrAbove1_21_2()) {
                    settings.blockLight(CoreReflections.field$BlockStateBase$lightBlock.getInt(blockState));
                    settings.propagatesSkylightDown(CoreReflections.field$BlockStateBase$propagatesSkylightDown.getBoolean(blockState) ? Tristate.TRUE : Tristate.FALSE);
                } else {
                    Object cache = CoreReflections.field$BlockStateBase$cache.get(blockState);
                    settings.blockLight(CoreReflections.field$BlockStateBase$Cache$lightBlock.getInt(cache));
                    settings.propagatesSkylightDown(CoreReflections.field$BlockStateBase$Cache$propagatesSkylightDown.getBoolean(cache) ? Tristate.TRUE : Tristate.FALSE);
                }
                this.vanillaBlockSettings[i] = settings;
            }
        } catch (Exception e) {
            this.plugin.logger().warn("Failed to initialize vanilla block settings", e);
        }
    }

    private BlockSounds toBlockSounds(Object soundType) throws ReflectiveOperationException {
        return new BlockSounds(
                toSoundData(CoreReflections.field$SoundType$breakSound.get(soundType), SoundData.SoundValue.FIXED_1, SoundData.SoundValue.FIXED_0_8),
                toSoundData(CoreReflections.field$SoundType$stepSound.get(soundType), SoundData.SoundValue.FIXED_0_15, SoundData.SoundValue.FIXED_1),
                toSoundData(CoreReflections.field$SoundType$placeSound.get(soundType), SoundData.SoundValue.FIXED_1, SoundData.SoundValue.FIXED_0_8),
                toSoundData(CoreReflections.field$SoundType$hitSound.get(soundType), SoundData.SoundValue.FIXED_0_5, SoundData.SoundValue.FIXED_0_5),
                toSoundData(CoreReflections.field$SoundType$fallSound.get(soundType), SoundData.SoundValue.FIXED_0_5, SoundData.SoundValue.FIXED_0_75)
        );
    }

    private SoundData toSoundData(Object soundEvent, SoundData.SoundValue volume, SoundData.SoundValue pitch) {
        Key soundId = KeyUtils.resourceLocationToKey(FastNMS.INSTANCE.field$SoundEvent$location(soundEvent));
        return new SoundData(soundId, volume, pitch);
    }

    private void initMirrorRegistry() {
        int size = RegistryUtils.currentBlockRegistrySize();
        BlockStateWrapper[] states = new BlockStateWrapper[size];
        for (int i = 0; i < size; i++) {
            states[i] = new BukkitBlockStateWrapper(BlockStateUtils.idToBlockState(i), i);
        }
        BlockRegistryMirror.init(states, new BukkitBlockStateWrapper(MBlocks.STONE$defaultState, BlockStateUtils.blockStateToId(MBlocks.STONE$defaultState)));
    }

    private void registerEmptyBlock() {
        Holder.Reference<CustomBlock> holder = ((WritableRegistry<CustomBlock>) BuiltInRegistries.BLOCK).registerForHolder(ResourceKey.create(BuiltInRegistries.BLOCK.key().location(), Key.withDefaultNamespace("empty")));
        EmptyBlock emptyBlock = new EmptyBlock(Key.withDefaultNamespace("empty"), holder);
        holder.bindValue(emptyBlock);
    }

    @Override
    protected CustomBlock.Builder platformBuilder(Key id) {
        return BukkitCustomBlock.builder(id);
    }

    // 注册服务端侧的真实方块
    private void registerServerSideCustomBlocks(int count) {
        // 这个会影响全局调色盘
        if (MCUtils.ceilLog2(this.vanillaBlockStateCount + count) == MCUtils.ceilLog2(this.vanillaBlockStateCount)) {
            PalettedContainer.NEED_DOWNGRADE = false;
        }
        try {
            unfreezeRegistry();
            for (int i = 0; i < count; i++) {
                Key customBlockId = BlockManager.createCustomBlockKey(i);
                DelegatingBlock customBlock;
                try {
                    customBlock = BlockGenerator.generateBlock(customBlockId);
                } catch (Throwable t) {
                    CraftEngine.instance().logger().warn("Failed to generate custom block " + customBlockId, t);
                    break;
                }
                this.customBlocks[i] = customBlock;
                try {
                    Object resourceLocation = KeyUtils.toResourceLocation(customBlockId);
                    Object blockHolder = CoreReflections.method$Registry$registerForHolder.invoke(null, MBuiltInRegistries.BLOCK, resourceLocation, customBlock);
                    this.customBlockHolders[i] = blockHolder;
                    CoreReflections.method$Holder$Reference$bindValue.invoke(blockHolder, customBlock);
                    CoreReflections.field$Holder$Reference$tags.set(blockHolder, Set.of());
                    DelegatingBlockState newBlockState = (DelegatingBlockState) FastNMS.INSTANCE.method$Block$defaultState(customBlock);
                    this.customBlockStates[i] = newBlockState;
                    CoreReflections.method$IdMapper$add.invoke(CoreReflections.instance$Block$BLOCK_STATE_REGISTRY, newBlockState);
                } catch (ReflectiveOperationException e) {
                    CraftEngine.instance().logger().warn("Failed to register custom block " + customBlockId, e);
                }
            }
        } finally {
            freezeRegistry();
        }
    }

    public Object cachedUpdateTagsPacket() {
        return this.cachedUpdateTagsPacket;
    }

    private void markVanillaNoteBlocks() {
        try {
            Object block = FastNMS.INSTANCE.method$Registry$getValue(MBuiltInRegistries.BLOCK, KeyUtils.toResourceLocation(BlockKeys.NOTE_BLOCK));
            Object stateDefinition = CoreReflections.field$Block$StateDefinition.get(block);
            @SuppressWarnings("unchecked")
            ImmutableList<Object> states = (ImmutableList<Object>) CoreReflections.field$StateDefinition$states.get(stateDefinition);
            CLIENT_SIDE_NOTE_BLOCKS.addAll(states);
        } catch (ReflectiveOperationException e) {
            this.plugin.logger().warn("Failed to init vanilla note block", e);
        }
    }

    @Override
    protected void setVanillaBlockTags(Key id, List<String> tags) {
        Object block = FastNMS.INSTANCE.method$Registry$getValue(MBuiltInRegistries.BLOCK, KeyUtils.toResourceLocation(id));
        this.clientBoundTags.put(FastNMS.INSTANCE.method$IdMap$getId(MBuiltInRegistries.BLOCK, block).orElseThrow(() -> new IllegalStateException("Block " + id + " not found")), tags);
    }

    public boolean isBlockSoundRemoved(Object block) {
        return this.replacedBlockSounds.contains(block);
    }

    private void unfreezeRegistry() {
        try {
            CoreReflections.field$MappedRegistry$frozen.set(MBuiltInRegistries.BLOCK, false);
            CoreReflections.field$MappedRegistry$unregisteredIntrusiveHolders.set(MBuiltInRegistries.BLOCK, new IdentityHashMap<>());
        } catch (IllegalAccessException e) {
            this.plugin.logger().warn("Failed to unfreeze block registry", e);
        }
    }

    private void freezeRegistry() {
        try {
            CoreReflections.field$MappedRegistry$frozen.set(MBuiltInRegistries.BLOCK, true);
        } catch (IllegalAccessException e) {
            this.plugin.logger().warn("Failed to freeze block registry", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void deceiveBukkitRegistry() {
        try {
            Map<Object, Material> magicMap = (Map<Object, Material>) CraftBukkitReflections.field$CraftMagicNumbers$BLOCK_MATERIAL.get(null);
            for (DelegatingBlock customBlock : this.customBlocks) {
                magicMap.put(customBlock, Material.STONE);
            }
        } catch (ReflectiveOperationException e) {
            this.plugin.logger().warn("Failed to deceive bukkit magic blocks", e);
        }
    }

    @Override
    protected boolean isVanillaBlock(Key id) {
        if (!id.namespace().equals("minecraft"))
            return false;
        if (id.value().equals("air"))
            return true;
        return FastNMS.INSTANCE.method$Registry$getValue(MBuiltInRegistries.BLOCK, KeyUtils.toResourceLocation(id)) != MBlocks.AIR;
    }

    public boolean isBurnable(Object blockState) {
        Object blockOwner = BlockStateUtils.getBlockOwner(blockState);
        return this.igniteOdds.getOrDefault(blockOwner, 0) > 0;
    }

    @Override
    public int vanillaBlockStateCount() {
        return this.vanillaBlockStateCount;
    }

    public boolean isOpenableBlockSoundRemoved(Object blockOwner) {
        return false;
    }

    public SoundData getRemovedOpenableBlockSound(Object blockOwner, boolean b) {
        return null;
    }
}
