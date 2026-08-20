package net.momirealms.craftengine.core.plugin.context.number;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.util.random.RandomUtils;

public record UniformNumberProvider(NumberProvider min, NumberProvider max) implements NumberProvider {
    public static final NumberProviderFactory<UniformNumberProvider> FACTORY = new Factory();

    @Override
    public int getInt(Context context) {
        return RandomUtils.generateRandomInt(this.min.getInt(context), this.max.getInt(context) + 1, context.random());
    }

    @Override
    public double getDouble(Context context) {
        return RandomUtils.generateRandomDouble(this.min.getDouble(context), this.max.getDouble(context), context.random());
    }

    @Override
    public float getFloat(Context context) {
        return RandomUtils.generateRandomFloat(this.min.getFloat(context), this.max.getFloat(context), context.random());
    }

    private static class Factory implements NumberProviderFactory<UniformNumberProvider> {

        @Override
        public UniformNumberProvider create(ConfigSection section) {
            return new UniformNumberProvider(
                    section.getNonNullNumber("min"),
                    section.getNonNullNumber("max")
            );
        }
    }
}
