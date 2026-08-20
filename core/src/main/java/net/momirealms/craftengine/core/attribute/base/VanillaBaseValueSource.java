package net.momirealms.craftengine.core.attribute.base;

import net.momirealms.craftengine.core.attribute.transform.ValueTransformer;
import net.momirealms.craftengine.core.attribute.transform.ValueTransformers;
import net.momirealms.craftengine.core.attribute.vanilla.VanillaAttributeInstance;
import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.entity.LivingEntity;
import net.momirealms.craftengine.core.plugin.config.ConfigKeys;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

public record VanillaBaseValueSource(
        Key attribute,
        double fallback,
        @Nullable ValueTransformer transformer,
        int updateInterval
) implements BaseValueSource {
    private static final String[] UPDATE_INTERVAL = ConfigKeys.of("update_interval");
    public static final BaseValueSourceFactory<VanillaBaseValueSource> FACTORY = args -> new VanillaBaseValueSource(
            args.getNonNullKey("attribute"),
            args.getDouble("fallback", 0d),
            args.getValue("transform", ValueTransformers::fromConfig),
            args.getValue(UPDATE_INTERVAL, value -> value.getAsInt(1), 20)
    );

    public VanillaBaseValueSource(Key attribute, double fallback, @Nullable ValueTransformer transformer) {
        this(attribute, fallback, transformer, 1);
    }

    @Override
    public double resolve(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            VanillaAttributeInstance attribute = livingEntity.getVanillaAttribute(this.attribute);
            if (attribute != null) {
                return transform(attribute.getBaseValue(), this.transformer);
            }
        }
        return this.fallback;
    }

    @Override
    public double resolveCurrent(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            VanillaAttributeInstance attribute = livingEntity.getVanillaAttribute(this.attribute);
            if (attribute != null) {
                return transform(attribute.getValue(), this.transformer);
            }
        }
        return this.fallback;
    }

    @Override
    public BaseValueSource bind(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            VanillaAttributeInstance attribute = livingEntity.getVanillaAttribute(this.attribute);
            if (attribute != null) {
                return new Bound(attribute, this.transformer, this.updateInterval);
            }
        }
        return new ConstantBaseValueSource(this.fallback);
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    private static double transform(double baseValue, @Nullable ValueTransformer transformer) {
        return transformer == null ? baseValue : transformer.transform(baseValue);
    }

    private record Bound(
            VanillaAttributeInstance attribute,
            @Nullable ValueTransformer transformer,
            int updateInterval
    ) implements BaseValueSource {

        @Override
        public double resolve(Entity entity) {
            return transform(this.attribute.getBaseValue(), this.transformer);
        }

        @Override
        public double resolveCurrent(Entity entity) {
            return transform(this.attribute.getValue(), this.transformer);
        }

        @Override
        public boolean isDynamic() {
            return true;
        }
    }
}
