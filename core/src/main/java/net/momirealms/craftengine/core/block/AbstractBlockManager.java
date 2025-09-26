package net.momirealms.craftengine.core.block;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElement;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElementConfig;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElementConfigs;
import net.momirealms.craftengine.core.block.properties.Properties;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.loot.LootTable;
import net.momirealms.craftengine.core.pack.LoadingSequence;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.pack.ResourceLocation;
import net.momirealms.craftengine.core.pack.model.generation.AbstractModelGenerator;
import net.momirealms.craftengine.core.pack.model.generation.ModelGeneration;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.*;
import net.momirealms.craftengine.core.plugin.context.event.EventFunctions;
import net.momirealms.craftengine.core.plugin.locale.LocalizedException;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.plugin.locale.TranslationManager;
import net.momirealms.craftengine.core.util.*;
import org.incendo.cloud.suggestion.Suggestion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

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
    // 声音映射表，和使用了哪些视觉方块有关
    protected final Map<Key, Key> soundReplacements = new HashMap<>(512, 0.5f);
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
        this.soundReplacements.clear();
        this.blockStateArranger.clear();
        this.appearanceToRealState.clear();
        Arrays.fill(this.blockStateMappings, -1);
        Arrays.fill(this.immutableBlockStates, EmptyBlock.STATE);
    }

    @Override
    public void delayedLoad() {
        this.initSuggestions();
        this.clearCache();
        this.resendTags();
    }

    @Override
    public Map<Key, CustomBlock> loadedBlocks() {
        return Collections.unmodifiableMap(this.byId);
    }

    @Override
    public Optional<CustomBlock> blockById(Key id) {
        return Optional.ofNullable(this.byId.get(id));
    }

    protected void addBlockInternal(Key id, CustomBlock customBlock) {
        ExceptionCollector<LocalizedResourceConfigException> exceptionCollector = new ExceptionCollector<>();
        // 绑定外观状态等
        for (ImmutableBlockState state : customBlock.variantProvider().states()) {
            int internalId = state.customBlockState().registryId();
            int appearanceId = state.vanillaBlockState().registryId();
            int index = internalId - this.vanillaBlockStateCount;
            ImmutableBlockState previous = this.immutableBlockStates[index];
            // todo 应当提前判断位置
            if (previous != null && !previous.isEmpty()) {
                exceptionCollector.add(new LocalizedResourceConfigException("warning.config.block.state.bind_failed",
                        state.toString(), previous.toString(), getBlockOwnerId(previous.customBlockState()).toString()));
                continue;
            }
            this.immutableBlockStates[index] = state;
            this.blockStateMappings[internalId] = appearanceId;
            this.appearanceToRealState.computeIfAbsent(appearanceId, k -> new IntArrayList()).add(internalId);
            // generate mod assets
            if (Config.generateModAssets()) {
                this.modBlockStateOverrides.put(getBlockOwnerId(state.customBlockState()), this.tempVanillaBlockStateModels.get(appearanceId));
            }
        }
        this.byId.put(id, customBlock);
        exceptionCollector.throwIfPresent();
    }

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

    protected abstract void resendTags();

    protected abstract boolean isVanillaBlock(Key id);

    protected abstract Key getBlockOwnerId(int id);

    protected abstract CustomBlock.Builder platformBuilder(Key id);

    protected abstract void setVanillaBlockTags(Key id, List<String> tags);

    public abstract int vanillaBlockStateCount();

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
            for (Map.Entry<String, Object> entry : section.entrySet()) {
                String before = entry.getKey();
                String after = entry.getValue().toString();
                // 先解析为唯一的wrapper
                BlockStateWrapper beforeState = createVanillaBlockState(before);
                BlockStateWrapper afterState = createVanillaBlockState(before);
                if (beforeState == null) {
                    TranslationManager.instance().log("warning.config.block_state_mapping.invalid_state", path.toString(), before);
                    return;
                }
                if (afterState == null) {
                    TranslationManager.instance().log("warning.config.block_state_mapping.invalid_state", path.toString(), after);
                    return;
                }
                int previous = AbstractBlockManager.this.blockStateMappings[beforeState.registryId()];
                if (previous != -1 && previous != afterState.registryId()) {
                    TranslationManager.instance().log("warning.config.block_state_mapping.conflict", path.toString(), beforeState.toString(), afterState.toString(), BlockRegistryMirror.byId(previous).toString());
                    return;
                }
                AbstractBlockManager.this.blockStateMappings[beforeState.registryId()] = afterState.registryId();
                AbstractBlockManager.this.blockStateArranger.computeIfAbsent(getBlockOwnerId(beforeState), k -> new ArrayList<>()).add(afterState);
            }
        }
    }

    public class BlockParser implements IdSectionConfigParser {
        public static final String[] CONFIG_SECTION_NAME = new String[]{"blocks", "block"};

        @Override
        public String[] sectionId() {
            return CONFIG_SECTION_NAME;
        }

        @Override
        public int loadingSequence() {
            return LoadingSequence.BLOCK;
        }

        @Override
        public void parseSection(Pack pack, Path path, Key id, Map<String, Object> section) {
            if (isVanillaBlock(id)) {
                parseVanillaBlock(pack, path, id, section);
            } else {
                // check duplicated config
                if (AbstractBlockManager.this.byId.containsKey(id)) {
                    throw new LocalizedResourceConfigException("warning.config.block.duplicate");
                }
                parseCustomBlock(pack, path, id, section);
            }
        }

        private void parseVanillaBlock(Pack pack, Path path, Key id, Map<String, Object> section) {
            Map<String, Object> settings = MiscUtils.castToMap(section.get("settings"), true);
            if (settings != null) {
                Object clientBoundTags = settings.get("client-bound-tags");
                if (clientBoundTags instanceof List<?> list) {
                    List<String> clientSideTags = MiscUtils.getAsStringList(list).stream().filter(ResourceLocation::isValid).toList();
                    AbstractBlockManager.this.setVanillaBlockTags(id, clientSideTags);
                }
            }
        }

        private void parseCustomBlock(Pack pack, Path path, Key id, Map<String, Object> section) {
            // 获取方块设置
            BlockSettings settings = BlockSettings.fromMap(id, MiscUtils.castToMap(section.get("settings"), true));
            // 读取基础外观配置
            Map<String, Property<?>> properties;
            Map<String, BlockStateAppearance> appearances;
            Map<String, BlockStateVariant> variants;
            // 读取states区域
            Map<String, Object> stateSection = MiscUtils.castToMap(ResourceConfigUtils.requireNonNullOrThrow(
                    ResourceConfigUtils.get(section, "state", "states"), "warning.config.block.missing_state"), true);
            boolean singleState = !stateSection.containsKey("properties");
            // 单方块状态
            if (singleState) {
                int internalId = ResourceConfigUtils.getAsInt(ResourceConfigUtils.requireNonNullOrThrow(stateSection.get("id"), "warning.config.block.state.missing_real_id"), "id");
                // 获取原版外观的注册表id
                BlockStateWrapper appearanceState = parsePluginFormattedBlockState(ResourceConfigUtils.requireNonEmptyStringOrThrow(stateSection.get("state"), "warning.config.block.state.missing_state"));
                Optional<BlockEntityElementConfig<? extends BlockEntityElement>[]> blockEntityRenderer = parseBlockEntityRender(stateSection.get("entity-renderer"));
                // 为原版外观赋予外观模型并检查模型冲突
                this.arrangeModelForStateAndVerify(appearanceState, ResourceConfigUtils.get(stateSection, "model", "models"));
                // 设置参数
                properties = Map.of();
                appearances = Map.of("", new BlockStateAppearance(appearanceState, blockEntityRenderer));
                variants = Map.of("", new BlockStateVariant("", settings, getInternalBlockState(internalId)));
            }
            // 多方块状态
            else {
                properties = parseBlockProperties(ResourceConfigUtils.getAsMap(ResourceConfigUtils.requireNonNullOrThrow(stateSection.get("properties"), "warning.config.block.state.missing_properties"), "properties"));
                appearances = parseBlockAppearances(ResourceConfigUtils.getAsMap(ResourceConfigUtils.requireNonNullOrThrow(stateSection.get("appearances"), "warning.config.block.state.missing_appearances"), "appearances"));
                variants = parseBlockVariants(
                        ResourceConfigUtils.getAsMap(ResourceConfigUtils.requireNonNullOrThrow(stateSection.get("variants"), "warning.config.block.state.missing_variants"), "variants"),
                        appearances::containsKey, settings
                );
            }

            addBlockInternal(id, platformBuilder(id)
                    .appearances(appearances)
                    .variantMapper(variants)
                    .properties(properties)
                    .settings(settings)
                    .lootTable(LootTable.fromMap(ResourceConfigUtils.getAsMapOrNull(section.get("loot"), "loot")))
                    .behavior(MiscUtils.getAsMapList(ResourceConfigUtils.get(section, "behavior", "behaviors")))
                    .events(EventFunctions.parseEvents(ResourceConfigUtils.get(section, "events", "event")))
                    .build());
        }

        private Map<String, BlockStateVariant> parseBlockVariants(Map<String, Object> variantsSection,
                                                                  Predicate<String> appearanceValidator,
                                                                  BlockSettings parentSettings) {
            Map<String, BlockStateVariant> variants = new HashMap<>();
            for (Map.Entry<String, Object> entry : variantsSection.entrySet()) {
                Map<String, Object> variantSection = ResourceConfigUtils.getAsMap(entry.getValue(), entry.getKey());
                String variantNBT = entry.getKey();
                String appearance = ResourceConfigUtils.requireNonEmptyStringOrThrow(variantSection.get("appearance"), "warning.config.block.state.variant.missing_appearance");
                if (!appearanceValidator.test(appearance)) {
                    throw new LocalizedResourceConfigException("warning.config.block.state.variant.invalid_appearance", variantNBT, appearance);
                }
                BlockStateWrapper internalBlockState = getInternalBlockState(ResourceConfigUtils.getAsInt(ResourceConfigUtils.requireNonNullOrThrow(variantSection.get("id"), "warning.config.block.state.missing_real_id"), "id"));
                Map<String, Object> anotherSetting = ResourceConfigUtils.getAsMapOrNull(variantSection.get("settings"), "settings");
                variants.put(variantNBT, new BlockStateVariant(appearance, anotherSetting == null ? parentSettings : BlockSettings.ofFullCopy(parentSettings, anotherSetting), internalBlockState));
            }
            return variants;
        }

        private BlockStateWrapper getInternalBlockState(int internalId) {
            if (internalId >= Config.serverSideBlocks()) {
                throw new LocalizedResourceConfigException("warning.config.block.state.invalid_real_id", BlockManager.createCustomBlockKey(internalId).asString(), String.valueOf(Config.serverSideBlocks() - 1));
            }
            return BlockRegistryMirror.byId(internalId + vanillaBlockStateCount());
        }

        private Map<String, BlockStateAppearance> parseBlockAppearances(Map<String, Object> appearancesSection) {
            Map<String, BlockStateAppearance> appearances = new HashMap<>();
            for (Map.Entry<String, Object> entry : appearancesSection.entrySet()) {
                Map<String, Object> appearanceSection = ResourceConfigUtils.getAsMap(entry.getValue(), entry.getKey());
                BlockStateWrapper appearanceId = parsePluginFormattedBlockState(ResourceConfigUtils.requireNonEmptyStringOrThrow(
                        appearanceSection.get("state"), "warning.config.block.state.missing_state"));
                this.arrangeModelForStateAndVerify(appearanceId, ResourceConfigUtils.get(appearanceSection, "model", "models"));
                appearances.put(entry.getKey(), new BlockStateAppearance(appearanceId, parseBlockEntityRender(appearanceSection.get("entity-renderer"))));
            }
            return appearances;
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
                    List<BlockStateWrapper> arranger =blockStateArranger.get(block);
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
