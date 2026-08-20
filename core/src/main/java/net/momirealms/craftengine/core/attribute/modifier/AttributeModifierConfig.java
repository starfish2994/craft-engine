package net.momirealms.craftengine.core.attribute.modifier;

import net.momirealms.craftengine.core.plugin.config.ConfigKeys;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;

import java.util.List;
import java.util.function.Predicate;

public class AttributeModifierConfig {
    protected static final String[] UPDATE_INTERVAL = ConfigKeys.of("update_interval");
    protected static final String[] CONDITIONS = ConfigKeys.of("condition(s)");
    public final Key attribute;
    public final Key id;
    public final NumberProvider amount;
    public final Key operation;
    public final Predicate<Context> condition;
    public final AttributeModifierScope scope;
    public final int updateInterval;
    public final boolean dynamic;

    public AttributeModifierConfig(Key attribute,
                                   Key id,
                                   NumberProvider amount,
                                   Key operation,
                                   Predicate<Context> condition,
                                   AttributeModifierScope scope,
                                   int updateInterval,
                                   boolean dynamic) {
        this.attribute = attribute;
        this.id = id;
        this.amount = amount;
        this.operation = operation;
        this.condition = condition;
        this.scope = scope;
        this.updateInterval = updateInterval;
        this.dynamic = dynamic;
    }

    public DynamicAttributeModifier build() {
        return new DynamicAttributeModifier(this);
    }

    @SuppressWarnings("DuplicatedCode")
    public static AttributeModifierConfig fromConfig(ConfigSection section) {
        Key attribute = section.getNonNullIdentifier("type");
        Key id = section.getNonNullIdentifier("id");
        AttributeModifierScope scope = section.getEnum("scope", AttributeModifierScope.class, AttributeModifierScope.ENTITY);
        Key operation = section.getNonNullIdentifier("operation");
        NumberProvider amount = section.getNonNullNumber("amount");
        List<Predicate<Context>> conditionsList = section.getList(CONDITIONS, CommonConditions::fromConfig);
        Predicate<Context> conditions = MiscUtils.allOf(conditionsList);
        boolean dynamic = !amount.isConstant() || !conditionsList.isEmpty();
        int updateInterval;
        if (dynamic) {
            updateInterval = section.getInt(UPDATE_INTERVAL, 20);
        } else {
            updateInterval = 0;
        }
        return new AttributeModifierConfig(attribute, id, amount, operation, conditions, scope, updateInterval, dynamic);
    }
}
