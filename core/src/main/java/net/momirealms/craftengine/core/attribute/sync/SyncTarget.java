package net.momirealms.craftengine.core.attribute.sync;

import net.momirealms.craftengine.core.attribute.vanilla.VanillaAttributeModifier;
import net.momirealms.craftengine.core.util.Key;

public record SyncTarget(Key target, VanillaAttributeModifier.Operation operation, SyncValueProvider valueProvider) {

    public double evaluate(double value, double base) {
        return this.valueProvider.resolve(value, base);
    }
}
