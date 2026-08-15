package net.momirealms.craftengine.core.attribute;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.KnownResourceException;
import net.momirealms.craftengine.core.util.Key;

public interface AttributeOperation {

    Key id();

    double apply(double phaseBase, double current, double amount);

    static AttributeOperation of(Key id, ApplyFunction function) {
        return new AttributeOperation() {
            @Override
            public Key id() {
                return id;
            }

            @Override
            public double apply(double phaseBase, double current, double amount) {
                return function.apply(phaseBase, current, amount);
            }

            @Override
            public String toString() {
                return "AttributeOperation{" + id.asString() + "}";
            }
        };
    }

    static AttributeOperation expression(Key id, String rawExpression) {
        Expression template = new Expression(rawExpression);
        try {
            template.copy().with("base", 0d).with("current", 0d).with("amount", 0d).evaluate();
        } catch (EvaluationException | ParseException e) {
            throw new KnownResourceException("attribute.operation.invalid_expression", id.asString(), rawExpression);
        } catch (ArithmeticException ignored) {
            // 零值探针触发的数学域错误（如除零）不代表表达式非法
        }
        return of(id, (base, current, amount) -> {
            try {
                return template.copy()
                        .with("base", base)
                        .with("current", current)
                        .with("amount", amount)
                        .evaluate().getNumberValue().doubleValue();
            } catch (EvaluationException | ParseException | ArithmeticException e) {
                CraftEngine.instance().logger().warn("Failed to evaluate attribute operation '" + id.asString() + "': " + rawExpression + " (" + e.getMessage() + ")");
                return current;
            }
        });
    }

    @FunctionalInterface
    interface ApplyFunction {

        double apply(double phaseBase, double current, double amount);
    }
}
