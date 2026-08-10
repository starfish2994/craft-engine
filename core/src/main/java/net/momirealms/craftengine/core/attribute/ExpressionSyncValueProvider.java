package net.momirealms.craftengine.core.attribute;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import net.momirealms.craftengine.core.plugin.CraftEngine;

public record ExpressionSyncValueProvider(Expression expression) implements SyncValueProvider {
    public static final SyncValueProviderFactory<ExpressionSyncValueProvider> FACTORY = args -> new ExpressionSyncValueProvider(new Expression(args.getNonNullString("expression")));
    public static final ExpressionSyncValueProvider DEFAULT = new ExpressionSyncValueProvider(new Expression("value"));

    @Override
    public double resolve(double value, double base) {
        synchronized (this.expression) {
            this.expression.with("value", value);
            this.expression.with("base", base);
            try {
                return this.expression.evaluate().getNumberValue().doubleValue();
            } catch (EvaluationException | ParseException e) {
                CraftEngine.instance().logger().warn("Failed to evaluate sync value expression: " + e.getMessage());
                return 0;
            }
        }
    }
}
