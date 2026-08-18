package net.momirealms.craftengine.core.attribute.base;

import net.momirealms.craftengine.core.entity.LivingEntity;

public record ConstantBaseValueSource(double value) implements BaseValueSource {
    public static final BaseValueSourceFactory<ConstantBaseValueSource> FACTORY = args -> new ConstantBaseValueSource(args.getDouble("value", 0d));

    @Override
    public double resolve(LivingEntity entity) {
        return this.value;
    }
}
