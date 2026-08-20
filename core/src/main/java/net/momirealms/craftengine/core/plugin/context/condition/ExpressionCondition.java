package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.ConfigKeys;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.number.PrecompiledExpression;

public final class ExpressionCondition<CTX extends Context> implements Condition<CTX> {
    private final PrecompiledExpression expression;

    private ExpressionCondition(String expression) {
        this.expression = new PrecompiledExpression(expression);
    }

    public static <CTX extends Context> ConditionFactory<CTX, ExpressionCondition<CTX>> factory() {
        return new Factory<>();
    }

    @Override
    public boolean test(CTX ctx) {
        try {
            return this.expression.evaluate(ctx).getBooleanValue();
        } catch (Throwable t) {
            CraftEngine.instance().logger().warn("Error evaluating expression: " + this.expression.raw(), t);
            return false;
        }
    }

    private static class Factory<CTX extends Context> implements ConditionFactory<CTX, ExpressionCondition<CTX>> {
        private static final String[] EXPR = ConfigKeys.of("expression|expr");

        @Override
        public ExpressionCondition<CTX> create(ConfigSection section) {
            return new ExpressionCondition<>(section.getNonNullString(EXPR));
        }
    }
}