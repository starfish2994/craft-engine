package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.Key;

public record VanillaDefaultBaseValueSource(Key attribute, double fallback) implements BaseValueSource {
    public static final BaseValueSourceFactory<VanillaDefaultBaseValueSource> FACTORY =
            args -> new VanillaDefaultBaseValueSource(args.getNonNullKey("attribute"), args.getDouble("fallback", 0d));

    @Override
    public double resolve(Entity entity) {
        return CraftEngine.instance().attributeManager().vanillaAttributeDefaultBaseValue(entity.type(), this.attribute, this.fallback);
    }
}
