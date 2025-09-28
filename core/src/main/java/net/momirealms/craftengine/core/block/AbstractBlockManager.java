package net.momirealms.craftengine.core.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.momirealms.craftengine.core.block.behavior.EntityBlockBehavior;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElement;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElementConfig;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElementConfigs;
import net.momirealms.craftengine.core.block.parser.BlockNbtParser;
import net.momirealms.craftengine.core.block.properties.Properties;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.loot.LootTable;
import net.momirealms.craftengine.core.pack.LoadingSequence;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.pack.PendingConfigSection;
import net.momirealms.craftengine.core.pack.ResourceLocation;
import net.momirealms.craftengine.core.pack.allocator.BlockStateAllocator;
import net.momirealms.craftengine.core.pack.allocator.IdAllocator;
import net.momirealms.craftengine.core.pack.allocator.BlockStateCandidate;
import net.momirealms.craftengine.core.pack.model.generation.AbstractModelGenerator;
import net.momirealms.craftengine.core.pack.model.generation.ModelGeneration;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.config.ConfigParser;
import net.momirealms.craftengine.core.plugin.config.IdSectionConfigParser;
import net.momirealms.craftengine.core.plugin.config.SectionConfigParser;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.plugin.context.event.EventFunctions;
import net.momirealms.craftengine.core.plugin.context.event.EventTrigger;
import net.momirealms.craftengine.core.plugin.context.function.Function;
import net.momirealms.craftengine.core.plugin.locale.LocalizedException;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.plugin.logger.Debugger;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Holder;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.*;
import net.momirealms.sparrow.nbt.CompoundTag;
import org.incendo.cloud.suggestion.Suggestion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public abstract class AbstractBlockManager extends AbstractModelGenerator implements BlockManager {
    protected final BlockParser blockParser;
    protected final BlockStateMappingParser blockStateMappingParser;
    // 根据id获取自定义方块
    protected final Map<Key, CustomBlock> byId = new HashMap<>();
    // 缓存的指令建议
    protected final List<Suggestion> cachedSuggestions = new ArrayList<>();
    // 缓存的使用中的命名空间
    protected final Set<String> namespacesInUse = new HashSet<>();
    // 用于检测单个外观方块状态是否被绑定了不同模型
    protected final Map<Integer, JsonElement> tempVanillaBlockStateModels = new Int2ObjectOpenHashMap<>();
    // Map<方块类型, Map<方块状态NBT,模型>>，用于生成block state json
    protected final Map<Key, Map<String, JsonElement>> blockStateOverrides = new HashMap<>();
    // 用于生成mod使用的block state json
    protected final Map<Key, JsonElement> modBlockStateOverrides = new HashMap<>();
    // 根据外观查找真实状态，用于debug指令
    protected final Map<Integer, List<Integer>> appearanceToRealState = new Int2ObjectOpenHashMap<>();
    // 用于note_block:0这样格式的自动分配
    protected final Map<Key, List<BlockStateWrapper>> blockStateArranger = new HashMap<>();
    // 全方块状态映射文件，用于网络包映射
    protected final int[] blockStateMappings;
    // 原版方块状态数量
    protected final int vanillaBlockStateCount;
    // 注册的大宝贝
    protected final DelegatingBlock[] customBlocks;
    protected final DelegatingBlockState[] customBlockStates;
    protected final Object[] customBlockHolders;
    // 自定义状态列表，会随着重载变化
    protected final ImmutableBlockState[] immutableBlockStates;
    // 原版方块的属性缓存
    protected final BlockSettings[] vanillaBlockSettings;
    // 倒推缓存
    protected final BlockStateCandidate[] reversedBlockStateArranger;
    // 临时存储哪些视觉方块被使用了
    protected final Set<BlockStateWrapper> tempVisualBlockStatesInUse = new HashSet<>();
    protected final Set<Key> tempVisualBlocksInUse = new HashSet<>();
    // 声音映射表，和使用了哪些视觉方块有关
    protected Map<Key, Key> soundReplacements = Map.of();

    protected AbstractBlockManager(CraftEngine plugin, int vanillaBlockStateCount, int customBlockCount) {
        super(plugin);
        this.vanillaBlockStateCount = vanillaBlockStateCount;
        this.blockParser = new BlockParser();
        this.blockStateMappingParser = new BlockStateMappingParser();
        this.customBlocks = new DelegatingBlock[customBlockCount];
        this.customBlockHolders = new Object[customBlockCount];
        this.customBlockStates = new DelegatingBlockState[customBlockCount];
        this.vanillaBlockSettings = new BlockSettings[vanillaBlockStateCount];
        this.immutableBlockStates = new ImmutableBlockState[customBlockCount];
        this.blockStateMappings = new int[customBlockCount + vanillaBlockStateCount];
        this.reversedBlockStateArranger = new BlockStateCandidate[vanillaBlockStateCount];
        Arrays.fill(this.blockStateMappings, -1);
    }

    @NotNull
    @Override
    public ImmutableBlockState getImmutableBlockStateUnsafe(int stateId) {
        return this.immutableBlockStates[stateId - this.vanillaBlockStateCount];
    }

    @Nullable
    @Override
    public ImmutableBlockState getImmutableBlockState(int stateId) {
        if (!isVanillaBlockState(stateId)) {
            return this.immutableBlockStates[stateId - this.vanillaBlockStateCount];
        }
        return null;
    }

    @Override
    public void unload() {
        super.clearModelsToGenerate();
        this.clearCache();
        this.cachedSuggestions.clear();
        this.namespacesInUse.clear();
        this.blockStateOverrides.clear();
        this.modBlockStateOverrides.clear();
        this.byId.clear();
        this.blockStateArranger.clear();
        this.appearanceToRealState.clear();
        Arrays.fill(this.blockStateMappings, -1);
        Arrays.fill(this.immutableBlockStates, EmptyBlock.STATE);
        Arrays.fill(this.reversedBlockStateArranger, null);
    }

    @Override
    public void delayedLoad() {
        this.initSuggestions();
        this.resendTags();
        this.processSounds();
        this.clearCache();
    }

    @Override
    public Map<Key, CustomBlock> loadedBlocks() {
        return Collections.unmodifiableMap(this.byId);
    }

    @Override
    public Optional<CustomBlock> blockById(Key id) {
        return Optional.ofNullable(this.byId.get(id));
    }

    protected abstract void applyPlatformSettings(ImmutableBlockState state);

    @Override
    public ConfigParser[] parsers() {
        return new ConfigParser[]{this.blockParser, this.blockStateMappingParser};
    }

    @Override
    public Map<Key, JsonElement> modBlockStates() {
        return Collections.unmodifiableMap(this.modBlockStateOverrides);
    }

    @Override
    public Map<Key, Map<String, JsonElement>> blockOverrides() {
        return Collections.unmodifiableMap(this.blockStateOverrides);
    }

    @Override
    public Collection<Suggestion> cachedSuggestions() {
        return Collections.unmodifiableCollection(this.cachedSuggestions);
    }

    @Nullable
    public Key replaceSoundIfExist(Key id) {
        return this.soundReplacements.get(id);
    }

    @Override
    public Map<Key, Key> soundReplacements() {
        return Collections.unmodifiableMap(this.soundReplacements);
    }

    public Set<String> namespacesInUse() {
        return Collections.unmodifiableSet(this.namespacesInUse);
    }

    protected void clearCache() {
        this.tempVanillaBlockStateModels.clear();
        this.tempVisualBlockStatesInUse.clear();
        this.tempVisualBlocksInUse.clear();
    }

    protected void initSuggestions() {
        this.cachedSuggestions.clear();
        this.namespacesInUse.clear();
        Set<String> states = new HashSet<>();
        for (CustomBlock block : this.byId.values()) {
            states.add(block.id().toString());
            this.namespacesInUse.add(block.id().namespace());
            for (ImmutableBlockState state : block.variantProvider().states()) {
                states.add(state.toString());
            }
        }
        for (String state : states) {
            this.cachedSuggestions.add(Suggestion.suggestion(state));
        }
    }

    @NotNull
    public List<Integer> appearanceToRealStates(int appearanceStateId) {
        return Optional.ofNullable(this.appearanceToRealState.get(appearanceStateId)).orElse(List.of());
    }

    public abstract BlockBehavior createBlockBehavior(CustomBlock customBlock, List<Map<String, Object>> behaviorConfig);

    protected abstract void resendTags();

    protected abstract boolean isVanillaBlock(Key id);

    protected abstract Key getBlockOwnerId(int id);

    protected abstract void setVanillaBlockTags(Key id, List<String> tags);

    protected abstract int vanillaBlockStateCount();

    protected abstract void processSounds();

    protected abstract CustomBlock createCustomBlock(@NotNull Holder.Reference<CustomBlock> holder,
                                                     @NotNull BlockStateVariantProvider variantProvider,
                                                     @NotNull Map<EventTrigger, List<Function<PlayerOptionalContext>>> events,
                                                     @Nullable LootTable<?> lootTable);

    public class BlockStateMappingParser implements SectionConfigParser {
        public static final String[] CONFIG_SECTION_NAME = new String[]{"block-state-mappings", "block-state-mapping"};

        @Override
        public String[] sectionId() {
            return CONFIG_SECTION_NAME;
        }

        @Override
        public int loadingSequence() {
            return LoadingSequence.BLOCK_STATE_MAPPING;
        }

        @Override
        public void parseSection(Pack pack, Path path, Map<String, Object> section) throws LocalizedException {
            ExceptionCollector<LocalizedResourceConfigException> exceptionCollector = new ExceptionCollector<>();
            for (Map.Entry<String, Object> entry : section.entrySet()) {
                String before = entry.getKey();
                String after = entry.getValue().toString();
                // 先解析为唯一的wrapper
                BlockStateWrapper beforeState = createVanillaBlockState(before);
                BlockStateWrapper afterState = createVanillaBlockState(after);
                if (beforeState == null) {
                    exceptionCollector.add(new LocalizedResourceConfigException("warning.config.block_state_mapping.invalid_state", before));
                    continue;
                }
                if (afterState == null) {
                    exceptionCollector.add(new LocalizedResourceConfigException("warning.config.block_state_mapping.invalid_state", after));
                    continue;
                }
                int previous = AbstractBlockManager.this.blockStateMappings[beforeState.registryId()];
                if (previous != -1 && previous != afterState.registryId()) {
                    exceptionCollector.add(new LocalizedResourceConfigException("warning.config.block_state_mapping.conflict",
                            beforeState.toString(),
                            afterState.toString(),
                            BlockRegistryMirror.byId(previous).toString()));
                    continue;
                }
                AbstractBlockManager.this.blockStateMappings[beforeState.registryId()] = afterState.registryId();
                Key blockOwnerId = getBlockOwnerId(beforeState);
                List<BlockStateWrapper> blockStateWrappers = AbstractBlockManager.this.blockStateArranger.computeIfAbsent(blockOwnerId, k -> new ArrayList<>());
                blockStateWrappers.add(beforeState);
                AbstractBlockManager.this.reversedBlockStateArranger[beforeState.registryId()] = blockParser.createVisualBlockCandidate(beforeState);

            }
            exceptionCollector.throwIfPresent();
        }
    }

    public class BlockParser implements IdSectionConfigParser {
        public static final String[] CONFIG_SECTION_NAME = new String[]{"blocks", "block"};
        private final IdAllocator internalIdAllocator;
        private final List<PendingConfigSection> pendingConfigSections = new ArrayList<>();
        private final BlockStateAllocator[] visualBlockStateAllocators = new BlockStateAllocator[AutoStateGroup.values().length];

        public BlockParser() {
            this.internalIdAllocator = new IdAllocator(AbstractBlockManager.this.plugin.dataFolderPath().resolve("cache").resolve("custom-block-states.json"));
        }

        public void addPendingConfigSection(PendingConfigSection section) {
            this.pendingConfigSections.add(section);
        }

        @Nullable
        public BlockStateCandidate createVisualBlockCandidate(BlockStateWrapper blockState) {
            List<AutoStateGroup> groups = AutoStateGroup.findGroups(blockState);
            if (!groups.isEmpty()) {
                BlockStateCandidate candidate = new BlockStateCandidate(blockState);
                for (AutoStateGroup group : groups) {
                    getOrCreateBlockStateAllocator(group).addCandidate(candidate);
                }
                return candidate;
            }
            return null;
        }

        private BlockStateAllocator getOrCreateBlockStateAllocator(AutoStateGroup group) {
            int index = group.ordinal();
            BlockStateAllocator visualBlockStateAllocator = this.visualBlockStateAllocators[index];
            if (visualBlockStateAllocator == null) {
                visualBlockStateAllocator = new BlockStateAllocator();
                this.visualBlockStateAllocators[index] = visualBlockStateAllocator;
            }
            return visualBlockStateAllocator;
        }

        @Override
        public void postProcess() {
            this.internalIdAllocator.processPendingAllocations();
            try {
                this.internalIdAllocator.saveToCache();
            } catch (IOException e) {
                AbstractBlockManager.this.plugin.logger().warn("Error while saving custom block state allocation", e);
            }
        }

        @Override
        public void preProcess() {
            this.internalIdAllocator.reset(0, Config.serverSideBlocks() - 1);
            try {
                this.internalIdAllocator.loadFromCache();
            } catch (IOException e) {
                AbstractBlockManager.this.plugin.logger().warn("Error while loading custom block state allocation cache", e);
            }
            for (PendingConfigSection section : this.pendingConfigSections) {
                ResourceConfigUtils.runCatching(
                        section.path(),
                        section.node(),
                        () -> parseSection(section.pack(), section.path(), section.node(), section.id(), section.config()),
                        () -> GsonHelper.get().toJson(section.config())
                );
            }
            this.pendingConfigSections.clear();
        }

        @Override
        public String[] sectionId() {
            return CONFIG_SECTION_NAME;
        }

        @Override
        public int loadingSequence() {
            return LoadingSequence.BLOCK;
        }

        @Override
        public void parseSection(Pack pack, Path path, String node, Key id, Map<String, Object> section) {
            if (isVanillaBlock(id)) {
                parseVanillaBlock(id, section);
            } else {
                // check duplicated config
                if (AbstractBlockManager.this.byId.containsKey(id)) {
                    throw new LocalizedResourceConfigException("warning.config.block.duplicate");
                }
                parseCustomBlock(path, node, id, section);
            }
        }

        private void parseVanillaBlock(Key id, Map<String, Object> section) {
            Map<String, Object> settings = MiscUtils.castToMap(section.get("settings"), true);
            if (settings != null) {
                Object clientBoundTags = settings.get("client-bound-tags");
                if (clientBoundTags instanceof List<?> list) {
                    List<String> clientSideTags = MiscUtils.getAsStringList(list).stream().filter(ResourceLocation::isValid).toList();
                    AbstractBlockManager.this.setVanillaBlockTags(id, clientSideTags);
                }
            }
        }

        private void parseCustomBlock(Path path, String node, Key id, Map<String, Object> section) {
            // 获取共享方块设置
            BlockSettings settings = BlockSettings.fromMap(id, MiscUtils.castToMap(section.get("settings"), true));
            // 读取states区域
            Map<String, Object> stateSection = MiscUtils.castToMap(ResourceConfigUtils.requireNonNullOrThrow(ResourceConfigUtils.get(section, "state", "states"), "warning.config.block.missing_state"), true);
            boolean singleState = !stateSection.containsKey("properties");
            // 读取方块的property，通过property决定
            Map<String, Property<?>> properties = singleState ? Map.of() : parseBlockProperties(ResourceConfigUtils.getAsMap(ResourceConfigUtils.requireNonNullOrThrow(stateSection.get("properties"), "warning.config.block.state.missing_properties"), "properties"));
            // 注册方块容器
            Holder.Reference<CustomBlock> holder = ((WritableRegistry<CustomBlock>) BuiltInRegistries.BLOCK).getOrRegisterForHolder(ResourceKey.create(BuiltInRegistries.BLOCK.key().location(), id));
            // 先绑定无效方块
            holder.bindValue(new InactiveCustomBlock(holder));

            // 根据properties生成variant provider
            BlockStateVariantProvider variantProvider = new BlockStateVariantProvider(holder, (owner, propertyMap) -> {
                ImmutableBlockState blockState = new ImmutableBlockState(owner, propertyMap);
                blockState.setSettings(settings);
                return blockState;
            }, properties);

            ImmutableList<ImmutableBlockState> states = variantProvider.states();
            List<CompletableFuture<Integer>> internalIdAllocators = new ArrayList<>(states.size());

            // 如果用户指定了起始id
            if (stateSection.containsKey("id")) {
                int startingId = ResourceConfigUtils.getAsInt(stateSection.get("id"), "id");
                int endingId = startingId + states.size() - 1;
                if (startingId < 0 || endingId >= Config.serverSideBlocks()) {
                    throw new LocalizedResourceConfigException("warning.config.block.state.invalid_id", startingId + "~" + endingId, String.valueOf(Config.serverSideBlocks() - 1));
                }
                // 先检测范围冲突
                List<Pair<String, Integer>> conflicts = this.internalIdAllocator.getFixedIdsBetween(startingId, endingId);
                if (!conflicts.isEmpty()) {
                    ExceptionCollector<LocalizedResourceConfigException> exceptionCollector = new ExceptionCollector<>();
                    for (Pair<String, Integer> conflict : conflicts) {
                        int internalId = conflict.right();
                        int index = internalId - startingId;
                        exceptionCollector.add(new LocalizedResourceConfigException("warning.config.block.state.id.conflict", states.get(index).toString(), conflict.left(), BlockManager.createCustomBlockKey(internalId).toString()));
                    }
                    exceptionCollector.throwIfPresent();
                }
                // 强行分配id
                for (ImmutableBlockState blockState : states) {
                    String blockStateId = blockState.toString();
                    internalIdAllocators.add(this.internalIdAllocator.assignFixedId(blockStateId, startingId++));
                }
            }
            // 未指定，则使用自动分配
            else {
                for (ImmutableBlockState blockState : states) {
                    String blockStateId = blockState.toString();
                    internalIdAllocators.add(this.internalIdAllocator.requestAutoId(blockStateId));
                }
            }

            CompletableFutures.allOf(internalIdAllocators).whenComplete((v, t) -> ResourceConfigUtils.runCatching(path, node, () -> {
                if (t != null) {
                    if (t instanceof CompletionException e) {
                        Throwable cause = e.getCause();
                        // 这里不会有conflict了，因为之前已经判断过了
                        if (cause instanceof IdAllocator.IdExhaustedException) {
                            throw new LocalizedResourceConfigException("warning.config.block.state.id.exhausted");
                        } else {
                            Debugger.BLOCK.warn(() -> "Unknown error while allocating internal block state id.", cause);
                            return;
                        }
                    }
                    throw new RuntimeException("Unknown error occurred", t);
                }

                for (int i = 0; i < internalIdAllocators.size(); i++) {
                    CompletableFuture<Integer> future = internalIdAllocators.get(i);
                    try {
                        int internalId = future.get();
                        states.get(i).setCustomBlockState(BlockRegistryMirror.byId(internalId + AbstractBlockManager.this.vanillaBlockStateCount));
                    } catch (InterruptedException | ExecutionException e) {
                        AbstractBlockManager.this.plugin.logger().warn("Interrupted while allocating internal block state for block " + id.asString(), e);
                        return;
                    }
                }

                // 创建自定义方块
                AbstractCustomBlock customBlock = (AbstractCustomBlock) createCustomBlock(
                        holder,
                        variantProvider,
                        EventFunctions.parseEvents(ResourceConfigUtils.get(section, "events", "event")),
                        LootTable.fromMap(ResourceConfigUtils.getAsMapOrNull(section.get("loot"), "loot"))
                );
                BlockBehavior blockBehavior = createBlockBehavior(customBlock, MiscUtils.getAsMapList(ResourceConfigUtils.get(section, "behavior", "behaviors")));

                // 单状态
                if (singleState) {
                    BlockStateWrapper appearanceState = parsePluginFormattedBlockState(ResourceConfigUtils.requireNonEmptyStringOrThrow(stateSection.get("state"), "warning.config.block.state.missing_state"));
                    this.arrangeModelForStateAndVerify(appearanceState, ResourceConfigUtils.get(stateSection, "model", "models"));
                    ImmutableBlockState onlyState = states.getFirst();
                    // 为唯一的状态绑定外观
                    onlyState.setVanillaBlockState(appearanceState);
                    parseBlockEntityRender(stateSection.get("entity-renderer")).ifPresent(onlyState::setConstantRenderers);
                } else {
                    BlockStateWrapper anyAppearanceState = null;
                    Map<String, Object> appearancesSection = ResourceConfigUtils.getAsMap(ResourceConfigUtils.requireNonNullOrThrow(stateSection.get("appearances"), "warning.config.block.state.missing_appearances"), "appearances");
                    // 也不能为空
                    if (appearancesSection.isEmpty()) {
                        throw new LocalizedResourceConfigException("warning.config.block.state.missing_appearances");
                    }
                    Map<String, BlockStateAppearance> appearances = Maps.newHashMap();
                    // 先解析所有的外观
                    for (Map.Entry<String, Object> entry : appearancesSection.entrySet()) {
                        Map<String, Object> appearanceSection = ResourceConfigUtils.getAsMap(entry.getValue(), entry.getKey());
                        // 解析对应的视觉方块
                        BlockStateWrapper appearanceState = parsePluginFormattedBlockState(ResourceConfigUtils.requireNonEmptyStringOrThrow(appearanceSection.get("state"), "warning.config.block.state.missing_state"));
                        this.arrangeModelForStateAndVerify(appearanceState, ResourceConfigUtils.get(appearanceSection, "model", "models"));
                        appearances.put(entry.getKey(), new BlockStateAppearance(appearanceState, parseBlockEntityRender(appearanceSection.get("entity-renderer"))));
                        if (anyAppearanceState == null) {
                            anyAppearanceState = appearanceState;
                        }
                    }
                    // 解析变体
                    Map<String, Object> variantsSection = ResourceConfigUtils.getAsMap(ResourceConfigUtils.requireNonNullOrThrow(stateSection.get("variants"), "warning.config.block.state.missing_variants"), "variants");
                    for (Map.Entry<String, Object> entry : variantsSection.entrySet()) {
                        Map<String, Object> variantSection = ResourceConfigUtils.getAsMap(entry.getValue(), entry.getKey());
                        String variantNBT = entry.getKey();
                        // 先解析nbt，找到需要修改的方块状态
                        CompoundTag tag = BlockNbtParser.deserialize(variantProvider, variantNBT);
                        if (tag == null) {
                            throw new LocalizedResourceConfigException("warning.config.block.state.property.invalid_format", variantNBT);
                        }
                        List<ImmutableBlockState> possibleStates = variantProvider.getPossibleStates(tag);
                        Map<String, Object> anotherSetting = ResourceConfigUtils.getAsMapOrNull(variantSection.get("settings"), "settings");
                        if (anotherSetting != null) {
                            for (ImmutableBlockState possibleState : possibleStates) {
                                possibleState.setSettings(BlockSettings.ofFullCopy(possibleState.settings(), anotherSetting));
                            }
                        }
                        String appearanceName = ResourceConfigUtils.getAsString(variantSection.get("appearance"));
                        if (appearanceName != null) {
                            BlockStateAppearance appearance = appearances.get(appearanceName);
                            if (appearance == null) {
                                throw new LocalizedResourceConfigException("warning.config.block.state.variant.invalid_appearance", variantNBT, appearanceName);
                            }
                            for (ImmutableBlockState possibleState : possibleStates) {
                                possibleState.setVanillaBlockState(appearance.blockState());
                                appearance.blockEntityRenderer().ifPresent(possibleState::setConstantRenderers);
                            }
                        }
                    }
                    // 为没有外观的方块状态填充
                    for (ImmutableBlockState blockState : states) {
                        if (blockState.vanillaBlockState() == null) {
                            blockState.setVanillaBlockState(anyAppearanceState);
                        }
                    }
                }

                // 获取方块实体行为
                EntityBlockBehavior entityBlockBehavior = blockBehavior.getEntityBehavior();
                boolean isEntityBlock = entityBlockBehavior != null;

                // 绑定行为
                for (ImmutableBlockState state : states) {
                    if (isEntityBlock) {
                        state.setBlockEntityType(entityBlockBehavior.blockEntityType());
                    }
                    state.setBehavior(blockBehavior);
                    int internalId = state.customBlockState().registryId();
                    BlockStateWrapper visualState = state.vanillaBlockState();
                    int appearanceId = visualState.registryId();
                    int index = internalId - AbstractBlockManager.this.vanillaBlockStateCount;
                    AbstractBlockManager.this.immutableBlockStates[index] = state;
                    AbstractBlockManager.this.blockStateMappings[internalId] = appearanceId;
                    AbstractBlockManager.this.appearanceToRealState.computeIfAbsent(appearanceId, k -> new IntArrayList()).add(internalId);
                    AbstractBlockManager.this.tempVisualBlockStatesInUse.add(visualState);
                    AbstractBlockManager.this.tempVisualBlocksInUse.add(getBlockOwnerId(visualState));
                    AbstractBlockManager.this.applyPlatformSettings(state);
                    // generate mod assets
                    if (Config.generateModAssets()) {
                        AbstractBlockManager.this.modBlockStateOverrides.put(BlockManager.createCustomBlockKey(index), Optional.ofNullable(AbstractBlockManager.this.tempVanillaBlockStateModels.get(appearanceId))
                                .orElseGet(() -> {
                                    // 如果未指定模型，说明复用原版模型？但是部分模型是多部位模型，无法使用变体解决问题
                                    // 未来需要靠mod重构彻底解决问题
                                    JsonObject json = new JsonObject();
                                    json.addProperty("model", "minecraft:block/air");
                                    return json;
                                }));
                    }
                }

                // 一定要到最后再绑定
                customBlock.setBehavior(blockBehavior);
                holder.bindValue(customBlock);

                // 添加方块
                AbstractBlockManager.this.byId.put(customBlock.id(), customBlock);

            }, () -> GsonHelper.get().toJson(section)));
        }

        @SuppressWarnings("unchecked")
        private Optional<BlockEntityElementConfig<? extends BlockEntityElement>[]> parseBlockEntityRender(Object arguments) {
            if (arguments == null) return Optional.empty();
            List<BlockEntityElementConfig<? extends BlockEntityElement>> blockEntityElementConfigs = ResourceConfigUtils.parseConfigAsList(arguments, BlockEntityElementConfigs::fromMap);
            if (blockEntityElementConfigs.isEmpty()) return Optional.empty();
            return Optional.of(blockEntityElementConfigs.toArray(new BlockEntityElementConfig[0]));
        }

        @NotNull
        private Map<String, Property<?>> parseBlockProperties(Map<String, Object> propertiesSection) {
            Map<String, Property<?>> properties = new HashMap<>();
            for (Map.Entry<String, Object> entry : propertiesSection.entrySet()) {
                Property<?> property = Properties.fromMap(entry.getKey(), ResourceConfigUtils.getAsMap(entry.getValue(), entry.getKey()));
                properties.put(entry.getKey(), property);
            }
            return properties;
        }

        private void arrangeModelForStateAndVerify(BlockStateWrapper blockStateWrapper, Object modelOrModels) {
            // 如果没有配置models
            if (modelOrModels == null) {
                return;
            }
            // 获取variants
            List<JsonObject> variants;
            if (modelOrModels instanceof String model) {
                JsonObject json = new JsonObject();
                json.addProperty("model", model);
                variants = Collections.singletonList(json);
            } else {
                variants = ResourceConfigUtils.parseConfigAsList(modelOrModels, this::parseAppearanceModelSectionAsJson);
                if (variants.isEmpty()) {
                    return;
                }
            }
            // 拆分方块id与属性
            String blockState = blockStateWrapper.toString();
            Key blockId = Key.of(blockState.substring(blockState.indexOf('{') + 1, blockState.lastIndexOf('}')));
            String propertyNBT = blockState.substring(blockState.indexOf('[') + 1, blockState.lastIndexOf(']'));
            // 结合variants
            JsonElement combinedVariant = GsonHelper.combine(variants);
            Map<String, JsonElement> overrideMap = AbstractBlockManager.this.blockStateOverrides.computeIfAbsent(blockId, k -> new HashMap<>());
            AbstractBlockManager.this.tempVanillaBlockStateModels.put(blockStateWrapper.registryId(), combinedVariant);
            JsonElement previous = overrideMap.get(propertyNBT);
            if (previous != null && !previous.equals(combinedVariant)) {
                throw new LocalizedResourceConfigException("warning.config.block.state.model.conflict", GsonHelper.get().toJson(combinedVariant), blockState, GsonHelper.get().toJson(previous));
            }
            overrideMap.put(propertyNBT, combinedVariant);
        }

        private JsonObject parseAppearanceModelSectionAsJson(Map<String, Object> section) {
            JsonObject json = new JsonObject();
            String modelPath = ResourceConfigUtils.requireNonEmptyStringOrThrow(section.get("path"), "warning.config.block.state.model.missing_path");
            if (!ResourceLocation.isValid(modelPath)) {
                throw new LocalizedResourceConfigException("warning.config.block.state.model.invalid_path", modelPath);
            }
            json.addProperty("model", modelPath);
            if (section.containsKey("x"))
                json.addProperty("x", ResourceConfigUtils.getAsInt(section.get("x"), "x"));
            if (section.containsKey("y"))
                json.addProperty("y", ResourceConfigUtils.getAsInt(section.get("y"), "y"));
            if (section.containsKey("uvlock")) json.addProperty("uvlock", ResourceConfigUtils.getAsBoolean(section.get("uvlock"), "uvlock"));
            if (section.containsKey("weight"))
                json.addProperty("weight", ResourceConfigUtils.getAsInt(section.get("weight"), "weight"));
            Map<String, Object> generationMap = MiscUtils.castToMap(section.get("generation"), true);
            if (generationMap != null) {
                prepareModelGeneration(ModelGeneration.of(Key.of(modelPath), generationMap));
            }
            return json;
        }

        // 从方块外观的state里获取其原版方块的state id
        private BlockStateWrapper parsePluginFormattedBlockState(String blockState) {
            // 五种合理情况
            // minecraft:note_block:10
            // note_block:10
            // minecraft:note_block[xxx=xxx]
            // note_block[xxx=xxx]
            // minecraft:barrier
            String[] split = blockState.split(":", 3);
            if (split.length >= 4) {
                throw new LocalizedResourceConfigException("warning.config.block.state.invalid_vanilla", blockState);
            }
            BlockStateWrapper wrapper;
            String stateOrId = split[split.length - 1];
            boolean isId = false;
            int arrangerIndex = 0;
            try {
                arrangerIndex = Integer.parseInt(stateOrId);
                if (arrangerIndex < 0) {
                    throw new LocalizedResourceConfigException("warning.config.block.state.invalid_vanilla", blockState);
                }
                isId = true;
            } catch (NumberFormatException ignored) {
            }
            // 如果末尾是id，则至少长度为2
            if (isId) {
                if (split.length == 1) {
                    throw new LocalizedResourceConfigException("warning.config.block.state.invalid_vanilla", blockState);
                }
                // 获取原版方块的id
                Key block = split.length == 2 ? Key.of(split[0]) : Key.of(split[0], split[1]);
                try {
                    List<BlockStateWrapper> arranger = AbstractBlockManager.this.blockStateArranger.get(block);
                    if (arranger == null) {
                        throw new LocalizedResourceConfigException("warning.config.block.state.unavailable_vanilla", blockState);
                    }
                    if (arrangerIndex >= arranger.size()) {
                        throw new LocalizedResourceConfigException("warning.config.block.state.invalid_vanilla_id", blockState, String.valueOf(arranger.size() - 1));
                    }
                    wrapper = arranger.get(arrangerIndex);
                } catch (NumberFormatException e) {
                    throw new LocalizedResourceConfigException("warning.config.block.state.invalid_vanilla", e, blockState);
                }
            } else {
                // 其他情况则是完整的方块
                BlockStateWrapper packedBlockState = createBlockState(blockState);
                if (packedBlockState == null) {
                    throw new LocalizedResourceConfigException("warning.config.block.state.invalid_vanilla", blockState);
                }
                wrapper = packedBlockState;
            }
            return wrapper;
        }
    }

    public boolean isVanillaBlockState(int id) {
        return id < this.vanillaBlockStateCount && id >= 0;
    }

    public BlockParser blockParser() {
        return blockParser;
    }

    public BlockStateMappingParser blockStateMappingParser() {
        return blockStateMappingParser;
    }
}
