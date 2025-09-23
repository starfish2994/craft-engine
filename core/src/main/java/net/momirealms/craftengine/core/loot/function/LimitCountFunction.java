package net.momirealms.craftengine.core.loot.function;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.loot.LootConditions;
import net.momirealms.craftengine.core.loot.LootContext;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LimitCountFunction<T> extends AbstractLootConditionalFunction<T> {
    public static final Factory<?> FACTORY = new Factory<>();
    @Nullable
    private final NumberProvider min;
    @Nullable
    private final NumberProvider max;

    public LimitCountFunction(List<Condition<LootContext>> predicates, @Nullable NumberProvider min, @Nullable NumberProvider max) {
        super(predicates);
        this.min = min;
        this.max = max;
    }

    @Override
    public Key type() {
        return LootFunctions.LIMIT_COUNT;
    }

    @Override
    protected Item<T> applyInternal(Item<T> item, LootContext context) {
        int amount = item.count();
        if (min != null) {
            int minAmount = min.getInt(context);
            if (amount < minAmount) {
                item.count(minAmount);
            }
        }
        if (max != null) {
            int maxAmount = max.getInt(context);
            if (amount > maxAmount) {
                item.count(maxAmount);
            }
        }
        return item;
    }

    public static class Factory<A> implements LootFunctionFactory<A> {
        @SuppressWarnings("unchecked")
        @Override
        public LootFunction<A> create(Map<String, Object> arguments) {
            Object min = arguments.get("min");
            Object max = arguments.get("max");
            List<Condition<LootContext>> conditions = Optional.ofNullable(arguments.get("conditions"))
                    .map(it -> LootConditions.fromMapList((List<Map<String, Object>>) it))
                    .orElse(Collections.emptyList());
            return new LimitCountFunction<>(conditions, min == null ? null : NumberProviders.fromObject(min), max == null ? null : NumberProviders.fromObject(max));
        }
    }
}
