package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.util.Key;

public record DamageFormulaType<T extends DamageFormula>(Key id, DamageFormulaFactory<T> factory) {
}
