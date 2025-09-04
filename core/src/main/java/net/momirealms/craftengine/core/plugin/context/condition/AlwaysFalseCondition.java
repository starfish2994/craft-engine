package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.util.Key;

import java.util.Map;

public class AlwaysFalseCondition<CTX extends Context> implements Condition<CTX> {

    @Override
    public Key type() {
        return CommonConditions.ALWAYS_FALSE;
    }

    public static class FactoryImpl<CTX extends Context> implements ConditionFactory<CTX> {

        @Override
        public Condition<CTX> create(Map<String, Object> arguments) {
            return new AlwaysFalseCondition<>();
        }
    }
}
