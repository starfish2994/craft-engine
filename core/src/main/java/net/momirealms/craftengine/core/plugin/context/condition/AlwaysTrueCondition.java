package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;

public final class AlwaysTrueCondition<CTX extends Context> implements Condition<CTX> {
    public static final AlwaysTrueCondition<Context> INSTANCE = new AlwaysTrueCondition<>();

    private AlwaysTrueCondition() {}

    @Override
    public boolean test(CTX ctx) {
        return true;
    }

    public static <CTX extends Context> ConditionFactory<CTX, AlwaysTrueCondition<CTX>> factory() {
        return new Factory<>();
    }

    @SuppressWarnings("unchecked")
    public static <CTX extends Context> AlwaysTrueCondition<CTX> instance() {
        return (AlwaysTrueCondition<CTX>) INSTANCE;
    }

    private static class Factory<CTX extends Context> implements ConditionFactory<CTX, AlwaysTrueCondition<CTX>> {

        @SuppressWarnings("unchecked")
        @Override
        public AlwaysTrueCondition<CTX> create(ConfigSection arguments) {
            return (AlwaysTrueCondition<CTX>) INSTANCE;
        }
    }
}