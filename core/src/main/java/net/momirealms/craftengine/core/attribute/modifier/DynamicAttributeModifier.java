package net.momirealms.craftengine.core.attribute.modifier;

import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.util.Key;

public class DynamicAttributeModifier implements AttributeModifier {
    private final AttributeModifierConfig config;

    public DynamicAttributeModifier(AttributeModifierConfig config) {
        this.config = config;
    }

    @Override
    public boolean test(Context context) {
        return this.config.condition.test(context);
    }

    @Override
    public Key id() {
        return this.config.id;
    }

    @Override
    public double amount(Context context) {
        return this.config.amount.getDouble(context);
    }

    @Override
    public Key operation() {
        return this.config.operation;
    }

    @Override
    public int updateInterval() {
        return this.config.updateInterval;
    }

    @Override
    public boolean isDynamic() {
        return this.config.dynamic;
    }
}
