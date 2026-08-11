package net.momirealms.craftengine.core.attribute.transform;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import net.momirealms.craftengine.core.plugin.CraftEngine;

public record ExpressionValueTransformer(Expression expression) implements ValueTransformer {
    public static final ValueTransformerFactory<ExpressionValueTransformer> FACTORY = args -> new ExpressionValueTransformer(new Expression(args.getString("expression", "value")));

    @Override
    public double transform(double value) {
        synchronized (this.expression) {
            this.expression.with("value", value);
            try {
                return this.expression.evaluate().getNumberValue().doubleValue();
            } catch (EvaluationException | ParseException e) {
                CraftEngine.instance().logger().warn("Failed to evaluate value transformer expression: " + e.getMessage());
                return value;
            }
        }
    }
}
