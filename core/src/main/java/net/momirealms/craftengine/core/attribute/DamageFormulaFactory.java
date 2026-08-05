package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;

public interface DamageFormulaFactory<T extends DamageFormula> {

    T create(ConfigSection args);
}
