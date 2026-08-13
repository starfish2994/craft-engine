package net.momirealms.craftengine.core.attribute;
import net.momirealms.craftengine.core.attribute.base.*;
import net.momirealms.craftengine.core.attribute.format.*;
import net.momirealms.craftengine.core.attribute.formula.*;
import net.momirealms.craftengine.core.attribute.sync.*;




import net.momirealms.craftengine.core.attribute.vanilla.VanillaAttributeModifier.Operation;
import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.*;
import net.momirealms.craftengine.core.plugin.config.lifecycle.LoadingStage;
import net.momirealms.craftengine.core.plugin.config.lifecycle.LoadingStages;
import net.momirealms.craftengine.core.util.ConcurrentChainedUUID2ReferenceHashTable;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;

public abstract class AbstractAttributeManager implements AttributeManager {
    protected final CraftEngine plugin;
    // API 注册
    protected final Map<Key, Attribute> apiAttributes = new HashMap<>();
    protected final Map<Key, AttributeOperation> apiOperations = new HashMap<>();
    // 配置注册
    protected final Map<Key, Attribute> configAttributes = new HashMap<>();
    protected final Map<Key, AttributeOperation> configOperations = new HashMap<>();
    // 运行中的实体属性容器
    protected final ConcurrentChainedUUID2ReferenceHashTable<AttributeContainer> containers = ConcurrentChainedUUID2ReferenceHashTable.createWithCapacity(128);
    private final OperationParser operationParser = new OperationParser();
    private final AttributeParser attributeParser = new AttributeParser();
    private final DamageFormulaParser damageFormulaParser = new DamageFormulaParser();
    // API配置合并
    protected Map<Key, Attribute> mergedAttributes = Map.of();
    // 按实体类型分桶的受限属性
    protected List<Attribute> globalAttributes = List.of();
    protected Map<Key, List<Attribute>> attributesByEntityType = Map.of();
    private CauseToFormula causeToFormula;

    protected AbstractAttributeManager(CraftEngine plugin) {
        this.plugin = plugin;
        this.apiOperations.put(AttributeOperations.ADD_VALUE_ID, AttributeOperations.ADD_VALUE);
        this.apiOperations.put(AttributeOperations.ADD_MULTIPLIED_BASE_ID, AttributeOperations.ADD_MULTIPLIED_BASE);
        this.apiOperations.put(AttributeOperations.ADD_MULTIPLIED_TOTAL_ID, AttributeOperations.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public void unload() {
        this.configAttributes.clear();
        this.configOperations.clear();
    }

    @Override
    public Optional<AttributeOperation> getOperation(Key id) {
        AttributeOperation operation = this.configOperations.get(id);
        return operation != null ? Optional.of(operation) : Optional.ofNullable(this.apiOperations.get(id));
    }

    @Override
    public Optional<Attribute> getAttribute(Key id) {
        return Optional.ofNullable(this.mergedAttributes.get(id));
    }

    @Override
    public Collection<Attribute> getAttributes() {
        return this.mergedAttributes.values();
    }

    @Override
    public List<Attribute> attributesByEntityType(Key entityType) {
        return this.attributesByEntityType.getOrDefault(entityType, this.globalAttributes);
    }

    @Override
    public double getAttributeValue(Entity entity, Attribute attribute) {
        AttributeGetter attributeGetter = getOrCreateContainer(entity);
        if (attributeGetter == null) {
            return attribute.defaultValue(entity);
        }
        return attributeGetter.getAttributeValue(attribute);
    }

    @Override
    public void removeContainer(UUID uuid) {
        AttributeContainer removed = this.containers.remove(uuid);
        if (removed instanceof AttributeContainer container) {
            container.clearSyncModifiers();
        }
    }

    @Override
    public AttributeContainer getContainer(UUID uuid) {
        return this.containers.get(uuid);
    }

    public AttributeContainer getContainer(Entity entity) {
        return this.containers.get(entity.uuid());
    }

    public AttributeContainer getOrCreateContainer(Entity entity) {
        return this.containers.computeIfAbsent(entity.uuid(), k -> new AttributeContainer(this, entity));
    }

    @Override
    public ConfigParser[] parsers() {
        return new ConfigParser[]{this.attributeParser, this.operationParser, this.damageFormulaParser};
    }

    @Override
    public DamageFormula findFormula(DamageEvent event) {
        if (this.causeToFormula == null) return null;
        return this.causeToFormula.getFormula(event);
    }

    @Override
    public void processDamageEvent(DamageEvent event) {
        DamageFormula formula = findFormula(event);
        if (formula == null) {
            return;
        }
        double newDamage = formula.getValue(event);
        if (event.damage() != newDamage) {
            if (newDamage < 0) {
                event.setDamage(0);
                return;
            }
            event.setDamage(newDamage);
        }
    }

    @Override
    public abstract double vanillaAttributeDefaultBaseValue(Key entityType, Key attribute, double fallback);

    private final class DamageFormulaParser extends SectionConfigParser {
        public static final String[] CONFIG_SECTION_NAME = new String[]{
                "damage_rule", "damage-rule",
                "damage_rules", "damage-rules",
        };

        @Override
        public Key type() {
            return Key.ce("damage_rule");
        }

        @Override
        protected void parseSection(Pack pack, Path path, ConfigSection section) {
            Map<Key, VictimToFormula> causeToFormulas = new HashMap<>();
            for (String damageSourceType : section.keySet()) {
                Key source = Key.of(damageSourceType);
                List<ConfigSection> sections = section.getList(damageSourceType, ConfigValue::getAsSection);
                Map<Key, DamageFormula> formulas = new HashMap<>();
                DamageFormula defaultFormula = null;
                for (int i = sections.size() - 1; i >= 0; i--) {
                    ConfigSection configSection = sections.get(i);
                    List<String> targets = configSection.getStringList("target");
                    DamageFormula formula = configSection.getNonNullValue("formula", ConfigConstants.ARGUMENT_STRING, DamageFormulas::fromConfig);
                    if (!targets.isEmpty()) {
                        for (String target : targets) {
                            if (target.isEmpty()) continue;
                            if (target.charAt(0) == '#') {
                                for (Key entity : AbstractAttributeManager.this.plugin.entityManager().entityIdsByTag(Key.of(target.substring(1)))) {
                                    formulas.put(entity, formula);
                                }
                            } else {
                                formulas.put(Key.of(target), formula);
                            }
                        }
                    } else {
                        defaultFormula = formula;
                    }
                }
                VictimToFormula victimToFormula = new VictimToFormula(defaultFormula, formulas);
                causeToFormulas.put(source, victimToFormula);
            }
            AbstractAttributeManager.this.causeToFormula = new CauseToFormula(causeToFormulas);
        }

        @Override
        public String[] sectionId() {
            return CONFIG_SECTION_NAME;
        }

        @Override
        public LoadingStage loadingStage() {
            return LoadingStages.ATTRIBUTE_RULES;
        }

        @Override
        public List<LoadingStage> dependencies() {
            return List.of(LoadingStages.ATTRIBUTE);
        }
    }

    private final class AttributeParser extends IdSectionConfigParser {
        public static final String[] CONFIG_SECTION_NAME = new String[]{"attributes", "attribute"};

        @Override
        public Key type() {
            return Key.ce("attribute");
        }

        @Override
        public String[] sectionId() {
            return CONFIG_SECTION_NAME;
        }

        @Override
        public LoadingStage loadingStage() {
            return LoadingStages.ATTRIBUTE;
        }

        @Override
        public List<LoadingStage> dependencies() {
            return List.of(LoadingStages.ENTITY, LoadingStages.ATTRIBUTE_OPERATION);
        }

        @Override
        public int count() {
            return AbstractAttributeManager.this.configAttributes.size();
        }

        @Override
        public void postProcess() {
            Map<Key, Attribute> attributes = new HashMap<>();
            attributes.putAll(AbstractAttributeManager.this.apiAttributes);
            attributes.putAll(AbstractAttributeManager.this.configAttributes);
            AbstractAttributeManager.this.mergedAttributes = attributes;
            List<Attribute> global = new ArrayList<>();
            Map<Key, List<Attribute>> byType = new HashMap<>();
            for (Attribute attribute : attributes.values()) {
                if (attribute.applicableEntityTypes == null) {
                    global.add(attribute);
                } else {
                    for (Key entityType : attribute.applicableEntityTypes) {
                        byType.computeIfAbsent(entityType, k -> new ArrayList<>()).add(attribute);
                    }
                }
            }
            AbstractAttributeManager.this.globalAttributes = List.copyOf(global);
            Map<Key, List<Attribute>> byTypeImmutable = new HashMap<>();
            byType.forEach((type, list) -> {
                List<Attribute> merged = new ArrayList<>(global.size() + list.size());
                merged.addAll(global);
                merged.addAll(list);
                byTypeImmutable.put(type, List.copyOf(merged));
            });
            AbstractAttributeManager.this.attributesByEntityType = Map.copyOf(byTypeImmutable);
        }

        @Override
        protected void parseSection(@NotNull Pack pack, @NotNull Path path, @NotNull Key id, @NotNull ConfigSection section) {
            BaseValueSource baseValueSource = section.<BaseValueSource>getValue("base", BaseValueSources::fromConfig, () -> new ConstantBaseValueSource(0d));
            ConfigSection constraintSection = section.getSection("constraint");
            ValueConstraint constraint = ValueConstraint.noLimit();
            if (constraintSection != null) {
                constraint = ValueConstraint.clamp(
                        constraintSection.getDouble("min", Double.MIN_VALUE),
                        constraintSection.getDouble("max", Double.MAX_VALUE)
                );
            }
            List<SyncTarget> sync = section.getSectionList("sync", v -> new SyncTarget(
                    v.getNonNullKey("target"),
                    v.getNonNullEnum("operation", Operation.class),
                    v.<SyncValueProvider>getValue("value", SyncValueProviders::fromConfig, () -> ExpressionSyncValueProvider.DEFAULT)
            ));
            ValueFormatter formatter = section.getValue("format", ValueFormatters::fromConfig);
            Set<Key> applicableEntityTypes = parseApplicableEntityTypes(section);
            List<AttributeOperation> operations = parseOperations(section);
            Attribute attribute = new Attribute(id, baseValueSource, constraint, operations, applicableEntityTypes, sync, formatter);
            AbstractAttributeManager.this.configAttributes.put(id, attribute);
        }
    }

    private List<AttributeOperation> parseOperations(ConfigSection section) {
        List<String> operationIds = section.getStringList("operations");
        if (operationIds.isEmpty()) return AttributeOperations.DEFAULT_PIPELINE;
        List<AttributeOperation> operations = new ArrayList<>(operationIds.size());
        for (String operationId : operationIds) {
            Key operationKey = Key.of(operationId);
            operations.add(getOperation(operationKey)
                    .orElseThrow(() -> new KnownResourceException("attribute.unknown_operation", section.assemblePath("operations"), operationKey.asString())));
        }
        return List.copyOf(operations);
    }

    @Nullable
    private Set<Key> parseApplicableEntityTypes(ConfigSection section) {
        List<String> entities = section.getStringList("entities");
        if (entities.isEmpty()) return null;
        Set<Key> types = new HashSet<>();
        for (String entry : entities) {
            if (entry.isEmpty()) continue;
            if (entry.charAt(0) == '#') {
                types.addAll(AbstractAttributeManager.this.plugin.entityManager().entityIdsByTag(Key.of(entry.substring(1))));
            } else {
                types.add(Key.of(entry));
            }
        }
        return types.isEmpty() ? null : Set.copyOf(types);
    }

    private final class OperationParser extends IdSectionConfigParser {
        public static final String[] CONFIG_SECTION_NAME = new String[]{
                "attribute_operation", "attribute-operation",
                "attribute_operations", "attribute-operations"
        };

        @Override
        public Key type() {
            return Key.ce("attribute_operation");
        }

        @Override
        public String[] sectionId() {
            return CONFIG_SECTION_NAME;
        }

        @Override
        public LoadingStage loadingStage() {
            return LoadingStages.ATTRIBUTE_OPERATION;
        }

        @Override
        public List<LoadingStage> dependencies() {
            return List.of();
        }

        @Override
        public int count() {
            return AbstractAttributeManager.this.configOperations.size();
        }

        @Override
        public boolean supportSearch() {
            return false;
        }

        @Override
        protected void parseSection(@NotNull Pack pack, @NotNull Path path, @NotNull Key id, @NotNull ConfigSection section) {
            String expression = section.getNonNullString("expression");
            AbstractAttributeManager.this.configOperations.put(id, AttributeOperation.expression(id, expression));
        }
    }
}
