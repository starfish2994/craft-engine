package net.momirealms.craftengine.core.attribute.formula;

import net.momirealms.craftengine.core.attribute.damage.DamageEvent;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class CauseToFormula {
    private final Map<Key, VictimToFormula> formulas;

    public CauseToFormula(Map<Key, VictimToFormula> formulas) {
        this.formulas = formulas;
    }

    @Nullable
    public DamageFormula getFormula(DamageEvent event) {
        VictimToFormula victimToFormula = this.formulas.get(event.source().type());
        if (victimToFormula != null) {
            return victimToFormula.getFormula(event.victim().id());
        }
        return null;
    }
}
