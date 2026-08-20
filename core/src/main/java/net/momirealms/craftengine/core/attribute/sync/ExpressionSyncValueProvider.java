package net.momirealms.craftengine.core.attribute.sync;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import net.momirealms.craftengine.core.plugin.CraftEngine;

public record ExpressionSyncValueProvider(Expression expression) implements SyncValueProvider {
    public static final SyncValueProviderFactory<ExpressionSyncValueProvider> FACTORY = args -> new ExpressionSyncValueProvider(new Expression(args.getNonNullString("expression")));
    public static final ExpressionSyncValueProvider DEFAULT = new ExpressionSyncValueProvider(new Expression("value"));

    @Override
    public double resolve(double value, double base) {
        try {
            return this.expression.copy()
                    .with("value", value)
                    .with("base", base)
                    .evaluate().getNumberValue().doubleValue();
        } catch (EvaluationException | ParseException e) {
            CraftEngine.instance().logger().warn("Failed to evaluate sync value expression: " + e.getMessage());
            return 0;
        }
    }
}
