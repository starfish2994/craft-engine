package net.momirealms.craftengine.core.attribute.formula;

import net.momirealms.craftengine.core.attribute.damage.DamageEvent;
import net.momirealms.craftengine.core.plugin.context.number.PrecompiledExpression;

import java.util.Map;

public class ExpressionDamageFormula implements DamageFormula {
    public static final DamageFormulaFactory<ExpressionDamageFormula> FACTORY = args -> compile(args.getNonNullString("expression"));

    private final String rawExpression;
    private final PrecompiledExpression compiled;

    private ExpressionDamageFormula(String rawExpression, PrecompiledExpression compiled) {
        this.rawExpression = rawExpression;
        this.compiled = compiled;
    }

    public static ExpressionDamageFormula compile(String formula) {
        PrecompiledExpression compiled = new PrecompiledExpression(formula);
        return new ExpressionDamageFormula(formula, compiled);
    }

    @Override
    public double getValue(DamageEvent event) {
        try {
            float attackStrength = event.attackStrength();
            return this.compiled.evaluate(event.context(), Map.of(
                    "damage", event.damage(),
                    "is_critical", event.source().isCritical(),
                    "is_sweep", event.isSweepAttack(),
                    "attack_strength", attackStrength,
                    "is_attack_ready", event.isAttackReady()
            )).getNumberValue().doubleValue();
        } catch (final RuntimeException e) {
            throw new RuntimeException("Failed to evaluate damage formula: " + this.rawExpression, e);
        }
    }
}
