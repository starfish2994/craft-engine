package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.attribute.vanilla.VanillaAttributeInstance;
import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.entity.LivingEntity;
import net.momirealms.craftengine.core.util.Key;

public record VanillaBaseValueSource(Key attribute, double fallback) implements BaseValueSource {
    public static final BaseValueSourceFactory<VanillaBaseValueSource> FACTORY = args -> new VanillaBaseValueSource(args.getNonNullKey("attribute"), args.getDouble("fallback", 0d));

    @Override
    public double resolve(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            VanillaAttributeInstance attribute = livingEntity.getVanillaAttribute(this.attribute);
            if (attribute != null) {
                return attribute.getBaseValue();
            }
        }
        return this.fallback;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }
}
