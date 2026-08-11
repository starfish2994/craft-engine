package net.momirealms.craftengine.core.attribute.base;
import net.momirealms.craftengine.core.attribute.transform.*;

import net.momirealms.craftengine.core.attribute.vanilla.VanillaAttributeInstance;
import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.entity.LivingEntity;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

public record VanillaBaseValueSource(Key attribute, double fallback, @Nullable ValueTransformer transformer) implements BaseValueSource {
    public static final BaseValueSourceFactory<VanillaBaseValueSource> FACTORY = args -> new VanillaBaseValueSource(
            args.getNonNullKey("attribute"),
            args.getDouble("fallback", 0d),
            args.getValue("transform", ValueTransformers::fromConfig)
    );

    @Override
    public double resolve(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            VanillaAttributeInstance attribute = livingEntity.getVanillaAttribute(this.attribute);
            if (attribute != null) {
                double baseValue = attribute.getBaseValue();
                return this.transformer == null ? baseValue : this.transformer.transform(baseValue);
            }
        }
        return this.fallback;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }
}
