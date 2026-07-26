package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;

public final class OpenWaterCondition<CTX extends Context> implements Condition<CTX> {

    private OpenWaterCondition() {
    }

    public static <CTX extends Context> ConditionFactory<CTX, OpenWaterCondition<CTX>> factory() {
        return new Factory<>();
    }

    @Override
    public boolean test(CTX ctx) {
        return ctx.getOptionalParameter(DirectContextParameters.OPEN_WATER).orElse(false);
    }

    private static class Factory<CTX extends Context> implements ConditionFactory<CTX, OpenWaterCondition<CTX>> {

        @Override
        public OpenWaterCondition<CTX> create(ConfigSection section) {
            return new OpenWaterCondition<>();
        }
    }
}
