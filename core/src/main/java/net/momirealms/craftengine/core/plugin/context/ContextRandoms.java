package net.momirealms.craftengine.core.plugin.context;

import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.util.random.RandomUtils;

import java.util.function.DoubleSupplier;

public final class ContextRandoms {
    private ContextRandoms() {}

    public static double getOrRoll(Context context, String id) {
        return getOrRoll(context, id, () -> RandomUtils.generateRandomDouble(0, 1));
    }

    public static double getOrRoll(Context context, String id, DoubleSupplier roll) {
        ContextHolder holder = context.contexts();
        NamedRandoms randoms = holder.getOrNull(DirectContextParameters.RANDOM);
        if (randoms == null) {
            if (holder.immutable()) {
                return roll.getAsDouble();
            }
            randoms = new NamedRandoms();
            holder.withParameter(DirectContextParameters.RANDOM, randoms);
        }
        return randoms.getOrRoll(id, roll);
    }
}
