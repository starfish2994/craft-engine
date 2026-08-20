package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigKeys;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.ContextRandoms;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.util.random.RandomUtils;
import org.jetbrains.annotations.Nullable;

public final class RandomCondition<CTX extends Context> implements Condition<CTX> {
    private final NumberProvider chance;
    private final String id;

    private RandomCondition(NumberProvider chance, @Nullable String id) {
        this.chance = chance;
        this.id = id;
    }

    @Override
    public boolean test(CTX ctx) {
        float chance = this.chance.getFloat(ctx);
        if (this.id != null) {
            return ContextRandoms.getOrRoll(ctx, this.id) < chance;
        }
        return RandomUtils.generateRandomFloat(0, 1) < chance;
    }

    public static <CTX extends Context> ConditionFactory<CTX, RandomCondition<CTX>> factory() {
        return new Factory<>();
    }

    private static class Factory<CTX extends Context> implements ConditionFactory<CTX, RandomCondition<CTX>> {
        private static final String[] ID = ConfigKeys.of("id");

        @Override
        public RandomCondition<CTX> create(ConfigSection section) {
            return new RandomCondition<>(
                    section.getNumber("value", ConfigConstants.CONSTANT_HALF),
                    section.getString(ID)
            );
        }
    }
}
