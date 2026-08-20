package net.momirealms.craftengine.core.attribute.formula;

import net.momirealms.craftengine.core.util.Key;

public record DamageFormulaType<T extends DamageFormula>(Key id, DamageFormulaFactory<T> factory) {
}
