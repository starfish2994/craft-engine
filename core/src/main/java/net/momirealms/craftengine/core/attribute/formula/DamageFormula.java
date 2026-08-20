package net.momirealms.craftengine.core.attribute.formula;

import net.momirealms.craftengine.core.attribute.damage.DamageEvent;

public interface DamageFormula {

    double getValue(DamageEvent event);
}
