package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

public record VanillaDefaultBaseValueSource(Key attribute, double fallback, @Nullable ValueTransformer transformer) implements BaseValueSource {
    public static final BaseValueSourceFactory<VanillaDefaultBaseValueSource> FACTORY = args -> new VanillaDefaultBaseValueSource(
            args.getNonNullKey("attribute"),
            args.getDouble("fallback", 0d),
            args.getValue("transform", ValueTransformers::fromConfig)
    );

    @Override
    public double resolve(Entity entity) {
        double baseValue = CraftEngine.instance().attributeManager().vanillaAttributeDefaultBaseValue(entity.type(), this.attribute, Double.NaN);
        if (Double.isNaN(baseValue)) return this.fallback;
        return this.transformer == null ? baseValue : this.transformer.transform(baseValue);
    }
}
