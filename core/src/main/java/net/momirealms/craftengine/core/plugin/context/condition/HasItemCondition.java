package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.util.ItemUtils;
import net.momirealms.craftengine.core.util.Key;

import java.util.Map;
import java.util.Optional;

public class HasItemCondition<CTX extends Context> implements Condition<CTX> {

    public HasItemCondition() {
    }

    @Override
    public Key type() {
        return CommonConditions.HAS_ITEM;
    }

    @Override
    public boolean test(CTX ctx) {
        Optional<Item<?>> item = ctx.getOptionalParameter(DirectContextParameters.ITEM_IN_HAND);
        if (item.isEmpty()) return false;
        Item<?> itemInHand = item.get();
        return !ItemUtils.isEmpty(itemInHand);
    }

    public static class FactoryImpl<CTX extends Context> implements ConditionFactory<CTX> {

        @Override
        public Condition<CTX> create(Map<String, Object> arguments) {
            return new HasItemCondition<>();
        }
    }
}
