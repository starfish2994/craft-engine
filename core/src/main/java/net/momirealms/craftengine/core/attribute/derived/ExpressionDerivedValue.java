package net.momirealms.craftengine.core.attribute.derived;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import net.momirealms.craftengine.core.attribute.Attribute;
import net.momirealms.craftengine.core.plugin.config.KnownResourceException;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ThrowableUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public final class ExpressionDerivedValue implements DerivedValue {
    public static final DerivedValueFactory<ExpressionDerivedValue> FACTORY = args -> compile(args.assemblePath("expression"), args.getNonNullString("expression"));
    private final String path;
    private final String rawExpression;
    private final Expression expression;
    private final List<VariableRef> variables;

    private ExpressionDerivedValue(String path, String rawExpression, Expression expression, List<VariableRef> variables) {
        this.path = path;
        this.rawExpression = rawExpression;
        this.expression = expression;
        this.variables = variables;
    }

    public static ExpressionDerivedValue compile(String path, String rawExpression) {
        Expression expression = new Expression(rawExpression);
        Set<String> usedVariables;
        try {
            usedVariables = expression.getUsedVariables();
        } catch (ParseException e) {
            ThrowableUtils.sneakyThrow(e);
            return null;
        }
        List<VariableRef> variables = new ArrayList<>(usedVariables.size());
        for (String variable : usedVariables) {
            variables.add(new VariableRef(variable, Key.of(variable.replaceFirst("_", ":"))));
        }
        return new ExpressionDerivedValue(path, rawExpression, expression, variables);
    }

    @Override
    public void bind(Function<Key, Attribute> resolver) {
        for (VariableRef variable : this.variables) {
            Attribute attribute = resolver.apply(variable.attributeId);
            if (attribute == null) {
                throw new KnownResourceException("attribute.derived.unknown_attribute", this.path, variable.attributeId.asString(), this.rawExpression);
            }
            variable.attribute = attribute;
        }
    }

    @Override
    public double evaluate(Function<Attribute, Double> resolver) {
        try {
            final Expression instance = this.expression.copy();
            for (VariableRef variable : this.variables) {
                instance.with(variable.name, resolver.apply(variable.attribute));
            }
            return instance.evaluate().getNumberValue().doubleValue();
        } catch (EvaluationException | ParseException | ArithmeticException e) {
            throw new RuntimeException("Failed to evaluate derived attribute expression: " + this.rawExpression, e);
        }
    }

    private static final class VariableRef {
        private final String name;
        private final Key attributeId;
        private Attribute attribute;

        private VariableRef(String name, Key attributeId) {
            this.name = name;
            this.attributeId = attributeId;
        }
    }
}
