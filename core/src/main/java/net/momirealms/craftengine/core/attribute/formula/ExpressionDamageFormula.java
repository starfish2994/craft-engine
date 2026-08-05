package net.momirealms.craftengine.core.attribute.formula;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import net.momirealms.craftengine.core.attribute.Attribute;
import net.momirealms.craftengine.core.attribute.AttributeSide;
import net.momirealms.craftengine.core.attribute.DamageEvent;
import net.momirealms.craftengine.core.attribute.DamageFormula;
import net.momirealms.craftengine.core.attribute.DamageFormulaFactory;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.KnownResourceException;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ExpressionDamageFormula implements DamageFormula {
    public static final String ATTACKER_PREFIX = "attacker_";
    public static final String VICTIM_PREFIX = "victim_";
    public static final DamageFormulaFactory<ExpressionDamageFormula> FACTORY = args -> compile(args.assemblePath("expression"), args.getNonNullString("expression"));
    private final String rawExpression;
    private final Expression expression;
    private final List<VariableBinding> bindings;

    public ExpressionDamageFormula(String rawExpression, Expression expression, List<VariableBinding> bindings) {
        this.rawExpression = rawExpression;
        this.expression = expression;
        this.bindings = bindings;
    }

    public static ExpressionDamageFormula compile(String path, String formula) {
        Expression expression = new Expression(formula);
        Set<String> usedVariables;
        try {
            usedVariables = expression.getUsedVariables();
        } catch (ParseException e) {
            throw new KnownResourceException("TODO", path, formula);
        }
        List<VariableBinding> bindings = new ArrayList<>();
        for (String variable : usedVariables) {
            switch (variable) {
                case "damage" ->
                        bindings.add(VariableBinding.field(variable, VariableBinding.FIELD_DAMAGE));
                case "is_critical" ->
                        bindings.add(VariableBinding.field(variable, VariableBinding.FIELD_IS_CRITICAL));
                default -> {
                    if (variable.startsWith(ATTACKER_PREFIX)) {
                        bindings.add(VariableBinding.attribute(variable, AttributeSide.ATTACKER, resolveAttribute(path, formula, variable, ATTACKER_PREFIX)));
                    } else if (variable.startsWith(VICTIM_PREFIX)) {
                        bindings.add(VariableBinding.attribute(variable, AttributeSide.VICTIM, resolveAttribute(path, formula, variable, VICTIM_PREFIX)));
                    } else {
                        throw new KnownResourceException("attribute.formula.unknown_variable", formula, variable);
                    }
                }
            }
        }
        return new ExpressionDamageFormula(formula, expression, bindings);
    }

    private static Attribute resolveAttribute(String path, String formula, String variable, String prefix) {
        return CraftEngine.instance().attributeManager().getAttribute(Key.of(variable.substring(prefix.length())))
                .orElseThrow(() -> new KnownResourceException("TODO", path, formula, variable));
    }

    @Override
    public double getValue(DamageEvent event) {
        synchronized (this.expression) {
            for (VariableBinding binding : this.bindings) {
                this.expression.with(binding.name(), binding.resolve(event));
            }
            try {
                return this.expression.evaluate().getNumberValue().doubleValue();
            } catch (EvaluationException | ParseException e) {
                throw new RuntimeException("Failed to evaluate damage formula: " + this.rawExpression, e);
            }
        }
    }

    public record VariableBinding(String name, @Nullable AttributeSide side, @Nullable Attribute attribute, int fieldKind) {
        static final int KIND_ATTRIBUTE = 0;
        static final int FIELD_DAMAGE = 1;
        static final int FIELD_IS_CRITICAL = 2;

        static VariableBinding field(String name, int fieldKind) {
            return new VariableBinding(name, null, null, fieldKind);
        }

        static VariableBinding attribute(String name, AttributeSide side, Attribute attribute) {
            return new VariableBinding(name, side, attribute, KIND_ATTRIBUTE);
        }

        double resolve(DamageEvent event) {
            return switch (this.fieldKind) {
                case FIELD_DAMAGE -> event.damage();
                case FIELD_IS_CRITICAL -> event.source().isCritical() ? 1d : 0d;
                default -> event.getAttributeValue(this.side, this.attribute);
            };
        }
    }
}
