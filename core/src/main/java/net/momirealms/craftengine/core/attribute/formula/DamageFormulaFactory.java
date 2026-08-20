package net.momirealms.craftengine.core.attribute.formula;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;

public interface DamageFormulaFactory<T extends DamageFormula> {

    T create(ConfigSection args);
}
