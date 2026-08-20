package net.momirealms.craftengine.bukkit.compatibility.worldedit;

import ca.spottedleaf.concurrentutil.map.concurrent.longs.ConcurrentChainedLong2ReferenceHashTable;
import com.fastasyncworldedit.bukkit.adapter.CachedBukkitAdapter;
import com.fastasyncworldedit.bukkit.adapter.FaweAdapter;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.extent.processor.ExtentBatchProcessorHolder;
import com.fastasyncworldedit.core.util.ExtentTraverser;
import com.fastasyncworldedit.core.util.ProcessorTraverser;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.MaskingExtent;
import com.sk89q.worldedit.function.RegionMaskingFilter;
import com.sk89q.worldedit.function.block.BlockReplace;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.BlockPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.visitor.RegionVisitor;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.plugin.injector.WorldStorageInjector;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.world.BukkitWorldManager;
import net.momirealms.craftengine.core.block.EmptyBlockDefinition;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.render.ConstantBlockEntityRenderer;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.LazyReference;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.ChunkPos;
import net.momirealms.craftengine.core.world.SectionPos;
import net.momirealms.craftengine.core.world.chunk.CEChunk;
import net.momirealms.craftengine.core.world.chunk.CESection;
import net.momirealms.craftengine.proxy.minecraft.server.level.ServerLevelProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.chunk.ChunkAccessProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.chunk.ChunkSourceProxy;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("deprecation")
final class FastAsyncWorldEditDelegate extends AbstractDelegateExtent {
    private static int[] ordinalToIbdID;
    // 兼任"本区块的 CEChunk 缓存"与"提交时需要落盘的区块集合"，避免对未加载区块逐块重复 readChunkAt
    private final ConcurrentChainedLong2ReferenceHashTable<CEChunk> chunksToSave;
    private final LazyReference<CEWorld> ceWorld;
    private final Set<ChunkPos> brokenChunks = Collections.synchronizedSet(new HashSet<>());

    private FastAsyncWorldEditDelegate(EditSessionEvent event, Extent extent) {
        super(extent);
        this.chunksToSave = ConcurrentChainedLong2ReferenceHashTable.createWithCapacity(16);
        this.ceWorld = LazyReference.untilNotNull(() -> {
            World weWorld = event.getWorld();
            requireNonNull(weWorld, "WorldEdit world is null");
            org.bukkit.World world = Bukkit.getWorld(weWorld.getName());
            requireNonNull(world, () -> "WorldEdit world " + weWorld.getName() + " is not a Bukkit world");
            // 有的插件可能刚开服就使用we操作，这时候ce还没对世界完成注入。就会无法拿到存储世界实例。
            CEWorld ceWorld = BukkitAdaptor.adapt(world).storageWorld();
            requireNonNull(ceWorld, () -> "WorldEdit world " + world.getName() + " is not a CraftEngine world");
            return ceWorld;
        });
    }

    static void init() {
        Settings.settings().EXTENT.ALLOWED_PLUGINS.add(FastAsyncWorldEditDelegate.class.getCanonicalName());
        try {
            Method ordinalToIbdIDMethod = CachedBukkitAdapter.class.getDeclaredMethod("getOrdinalToIbdID"); // 这样获取有代码提示
            ordinalToIbdIDMethod.setAccessible(true);
            if (WorldEditPlugin.getInstance().getBukkitImplAdapter() instanceof FaweAdapter<?, ?> faweAdapter) { // 确保是 paper 服务器的才调用这个
                ordinalToIbdID = (int[]) ordinalToIbdIDMethod.invoke(faweAdapter);
            } else { // 应该在 spigot 的服务器上用不了这个
                CraftEngine.instance().logger().warn("Failed to init FastAsyncWorldEdit compatibility, Please use the server of paper or its fork.");
                return;
            }
        } catch (ReflectiveOperationException e) {
            CraftEngine.instance().logger().warn("Failed to init FastAsyncWorldEdit compatibility", e);
            return;
        }
        WorldEdit.getInstance().getEventBus().register(new Object() {
            @Subscribe
            @SuppressWarnings("unused")
            public void onEditSessionEvent(EditSessionEvent event) {
                if (event.getWorld() == null || event.getStage() != EditSession.Stage.BEFORE_CHANGE) return;
                if (!BukkitWorldManager.instance().initialized()) return;
                event.setExtent(new FastAsyncWorldEditDelegate(event, event.getExtent()));
            }
        });
    }

    private static void injectLevelChunk(Object chunkSource, CEChunk ceChunk) {
        ChunkPos pos = ceChunk.chunkPos();
        Object levelChunk = ChunkSourceProxy.INSTANCE.getChunk(chunkSource, pos.x, pos.z, false);
        if (levelChunk != null) {
            Object[] sections = ChunkAccessProxy.INSTANCE.getSections(levelChunk);
            CESection[] ceSections = ceChunk.sections();
            synchronized (sections) {
                for (int i = 0; i < ceSections.length; i++) {
                    CESection ceSection = ceSections[i];
                    Object section = sections[i];
                    int finalI = i;
                    WorldStorageInjector.inject(section, ceSection, ceChunk, new SectionPos(pos.x, ceChunk.sectionY(i), pos.z),
                            (injected) -> sections[finalI] = injected);
                }
            }
        }
    }

    private CEWorld ceWorld() {
        return this.ceWorld.get();
    }

    // 批量方法不能转发给下游：尾部队列上 Extent 的默认实现迭代的是队列自身的 setBlock，
    // 本类的记录逻辑会被完全绕过。这里逐块路由经过自身的 setBlock，记录到的即为真实修改。
    // 已核实 SingleThreadQueueExtent / ParallelQueueExtent / BukkitWorld 的批量方法均为
    // Extent 默认实现或等价逻辑，不会因此丢失下游优化。

    @Override
    public int setBlocks(final Set<BlockVector3> vset, final Pattern pattern) {
        if (vset instanceof Region region) {
            return setBlocks(region, pattern);
        }
        int count = 0;
        for (BlockVector3 pos : vset) {
            if (pattern.apply(this, pos, pos)) {
                count++;
            }
        }
        return count;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public int setBlocks(final Region region, final Pattern pattern) {
        // 与 Extent 默认实现相同的快速路径
        if (pattern instanceof BlockPattern blockPattern) {
            return setBlocks(region, blockPattern.getBlock());
        }
        if (pattern instanceof BlockStateHolder) {
            return setBlocks(region, (BlockStateHolder) pattern);
        }
        int count = 0;
        for (BlockVector3 pos : region) {
            if (pattern.apply(this, pos, pos)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public <B extends BlockStateHolder<B>> int setBlocks(final Region region, final B block) {
        int count = 0;
        for (BlockVector3 pos : region) {
            if (setBlock(pos, block)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public int replaceBlocks(Region region, Mask mask, Pattern pattern) {
        BlockReplace replace = new BlockReplace(this, pattern);
        RegionMaskingFilter filter = new RegionMaskingFilter(mask, replace);
        RegionVisitor visitor = new RegionVisitor(region, filter, this);
        Operations.completeLegacy(visitor);
        return visitor.getAffected();
    }

    @Override
    public <B extends BlockStateHolder<B>> int replaceBlocks(final Region region, final Set<BaseBlock> filter, final B replacement) {
        return replaceBlocks(region, filter, (Pattern) replacement);
    }

    @Override
    public int replaceBlocks(final Region region, final Set<BaseBlock> filter, final Pattern pattern) {
        // filter 匹配的是被替换的旧方块，统一转换为 mask 语义（mask.test 读取的是旧方块）
        Mask mask = filter == null ? new ExistingBlockMask(this) : new BlockMask(this, filter);
        return replaceBlocks(region, mask, pattern);
    }

    @Override
    public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 position, T block) {
        // 不走 int 变体，忠实地按原变体转发：下游（如 MaskingExtent、BlockChangeLimiter 及
        // 其他插件在 BEFORE_CHANGE 注册的 extent）普遍只重写 BlockVector3 变体
        Mask mask = getMask();
        if (mask != null && !mask.test(position)) {
            return super.setBlock(position, block);
        }
        // 必须在修改发生前读取旧方块，否则读到的会是新状态
        BaseBlock oldBlock = getBlock(position).toBaseBlock();
        if (!super.setBlock(position, block)) {
            return false;
        }
        this.processBlock(position.x(), position.y(), position.z(), block.toBaseBlock(), oldBlock);
        return true;
    }

    @Override
    public <T extends BlockStateHolder<T>> boolean setBlock(int x, int y, int z, T block) {
        // mask 在队列模式下是 flush 时由 MaskingExtent 的 IBatchProcessor 生效的（它不在链上），
        // 链上 setBlock 对被掩码位置依然返回 true，这里必须自行过滤以免误记录
        Mask mask = getMask();
        if (mask != null && !mask.test(BlockVector3.at(x, y, z))) {
            return super.setBlock(x, y, z, block);
        }
        // 必须在修改发生前读取旧方块，否则读到的会是新状态
        BaseBlock oldBlock = getBlock(x, y, z).toBaseBlock();
        if (!super.setBlock(x, y, z, block)) {
            return false;
        }
        this.processBlock(x, y, z, block.toBaseBlock(), oldBlock);
        return true;
    }

    // 与 EditSession.setMask 相同的双重查找：MaskingExtent 可能是链节点（wnaMode），
    // 也可能只存在于队列的 processor 复合体中（队列模式）
    public Mask getMask() {
        MaskingExtent maskingExtent = new ExtentTraverser<>(getExtent()).findAndGet(MaskingExtent.class);
        if (maskingExtent == null) {
            ExtentBatchProcessorHolder processorExtent =
                    new ExtentTraverser<>(getExtent()).findAndGet(ExtentBatchProcessorHolder.class);
            if (processorExtent != null) {
                maskingExtent =
                        new ProcessorTraverser<>(processorExtent.getProcessor()).find(MaskingExtent.class);
            }
        }
        return maskingExtent != null ? maskingExtent.getMask() : null;
    }

    @Override
    public @Nullable Operation commit() {
        saveAllChunks();
        Operation operation = super.commit();
        List<ChunkPos> chunks = new ArrayList<>(this.brokenChunks);
        this.brokenChunks.clear();
        Object worldServer = this.ceWorld().world().minecraftWorld();
        Object chunkSource = ServerLevelProxy.INSTANCE.getChunkSource(worldServer);
        for (ChunkPos chunk : chunks) {
            CEChunk loaded = this.ceWorld().getChunkAtIfLoaded(chunk.longKey());
            // only inject loaded chunks
            if (loaded == null) continue;
            injectLevelChunk(chunkSource, loaded);
        }
        return operation;
    }

    private void processBlock(int blockX, int blockY, int blockZ, BaseBlock newBlock, BaseBlock oldBlock) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        int newStateId = ordinalToIbdID[newBlock.getOrdinal()];
        int oldStateId = ordinalToIbdID[oldBlock.getOrdinal()];
        this.brokenChunks.add(ChunkPos.of(chunkX, chunkZ));
        if (BlockStateUtils.isVanillaBlock(newStateId) && BlockStateUtils.isVanillaBlock(oldStateId)) return;
        try {
            long chunkKey = ChunkPos.asLong(chunkX, chunkZ);
            CEChunk ceChunk = this.chunksToSave.get(chunkKey);
            if (ceChunk == null) {
                CEChunk loaded = this.ceWorld().getChunkAtIfLoaded(chunkX, chunkZ);
                ceChunk = loaded != null ? loaded : this.ceWorld().worldDataStorage().readChunkAt(this.ceWorld(), new ChunkPos(chunkX, chunkZ), null); // todo fix pdc storage type
                this.chunksToSave.put(chunkKey, ceChunk);
            }
            CESection ceSection = ceChunk.sectionById(SectionPos.blockToSectionCoord(blockY));
            ImmutableBlockState newImmutableBlockState = BukkitBlockManager.instance().getImmutableBlockState(newStateId);
            ImmutableBlockState oldImmutableBlockState = BukkitBlockManager.instance().getImmutableBlockState(oldStateId);
            if (newImmutableBlockState == null) {
                ceSection.setBlockState(blockX & 15, blockY & 15, blockZ & 15, EmptyBlockDefinition.STATE);
            } else {
                ceSection.setBlockState(blockX & 15, blockY & 15, blockZ & 15, newImmutableBlockState);
            }
            ceChunk.setUnsaved(true);
            BlockPos pos = new BlockPos(blockX, blockY, blockZ);
            // 旧方块实体：新状态换了主人（或变为原版块）则移除
            if (oldImmutableBlockState != null && oldImmutableBlockState.hasBlockEntity() && (newImmutableBlockState == null || oldImmutableBlockState.owner() != newImmutableBlockState.owner())) {
                BlockEntity blockEntity = ceChunk.getBlockEntity(pos, false);
                if (blockEntity != null) {
                    ceChunk.removeBlockEntity(pos);
                }
            }
            // 旧常量渲染器：新状态没有渲染器则直接移除并隐藏，否则暂存用于平滑转换
            ConstantBlockEntityRenderer previousRenderer = null;
            if (oldImmutableBlockState != null && oldImmutableBlockState.hasConstantBlockEntityRenderer()) {
                previousRenderer = ceChunk.removeConstantBlockEntityRenderer(pos, newImmutableBlockState == null || !newImmutableBlockState.hasConstantBlockEntityRenderer());
            }
            // 新方块实体
            if (newImmutableBlockState != null && newImmutableBlockState.hasBlockEntity()) {
                BlockEntity blockEntity = ceChunk.getBlockEntity(pos, false);
                if (blockEntity == null) {
                    ceChunk.addBlockEntity(new BlockEntity(pos, newImmutableBlockState));
                } else {
                    blockEntity.setBlockState(newImmutableBlockState);
                    // 方块类型未变，仅更新状态，选择性更新ticker
                    if (ceChunk.isActivated()) {
                        ceChunk.replaceOrCreateTickingBlockEntity(blockEntity);
                        ceChunk.createDynamicBlockEntityRenderer(blockEntity);
                    }
                }
            }
            // 新常量渲染器
            if (newImmutableBlockState != null && newImmutableBlockState.hasConstantBlockEntityRenderer()) {
                ceChunk.addConstantBlockEntityRenderer(pos, newImmutableBlockState, previousRenderer);
            }
        } catch (IOException e) {
            CraftEngine.instance().logger().warn("Error when recording FastAsyncWorldEdit operation blocks", e);
        }
    }

    private void saveAllChunks() {
        try {
            for (CEChunk ceChunk : this.chunksToSave.values()) {
                this.ceWorld().worldDataStorage().writeChunkAt(ceChunk.chunkPos(), ceChunk);
            }
            this.chunksToSave.clear();
        } catch (IOException e) {
            CraftEngine.instance().logger().warn("Error when recording FastAsyncWorldEdit operation chunks", e);
        }
    }
}
