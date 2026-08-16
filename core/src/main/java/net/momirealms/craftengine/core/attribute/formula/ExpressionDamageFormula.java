package net.momirealms.craftengine.core.attribute.formula;

import net.momirealms.craftengine.core.plugin.config.KnownResourceException;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.number.PrecompiledExpression;

import java.util.Set;

public class ExpressionDamageFormula implements DamageFormula {
    public static final DamageFormulaFactory<ExpressionDamageFormula> FACTORY = args -> compile(args.getNonNullString("expression"));
    private static final Set<String> EVENT_VARIABLES = Set.of("damage", "is_critical", "is_sweep");

    private final String rawExpression;
    private final PrecompiledExpression compiled;

    private ExpressionDamageFormula(String rawExpression, PrecompiledExpression compiled) {
        this.rawExpression = rawExpression;
        this.compiled = compiled;
    }

    public static ExpressionDamageFormula compile(String formula) {
        PrecompiledExpression compiled = new PrecompiledExpression(formula);
        for (String variable : compiled.usedVariables()) {
            if (!EVENT_VARIABLES.contains(variable)) {
                throw new KnownResourceException("attribute.formula.unknown_variable", formula, variable);
            }
        }
        return new ExpressionDamageFormula(formula, compiled);
    }

    @Override
    public double getValue(DamageEvent event) {
        try {
            return this.compiled.evaluate(EntityDamageContext.of(event, ContextHolder.builder()

            )).getNumberValue().doubleValue();
        } catch (final RuntimeException e) {
            throw new RuntimeException("Failed to evaluate damage formula: " + this.rawExpression, e);
        }
    }
}
