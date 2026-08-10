package net.momirealms.craftengine.core.attribute.vanilla;

import net.momirealms.craftengine.core.util.Key;

public interface VanillaAttributeInstance {

    double getValue();

    double getBaseValue();

    void setBaseValue(double baseValue);

    void addOrUpdateTransientModifier(Key id, VanillaAttributeModifier.Operation operation, double amount);

    void removeModifier(Key id);
}
