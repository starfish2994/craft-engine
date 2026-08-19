package net.momirealms.craftengine.core.attribute.base;

import net.momirealms.craftengine.core.attribute.transform.ValueTransformer;
import net.momirealms.craftengine.core.attribute.transform.ValueTransformers;
import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

public record ByEntityTypeBaseValueSource(Key attribute, double fallback, @Nullable ValueTransformer transformer) implements BaseValueSource {
    public static final BaseValueSourceFactory<ByEntityTypeBaseValueSource> FACTORY = args -> new ByEntityTypeBaseValueSource(
            args.getNonNullKey("attribute"),
            args.getDouble("fallback", 0d),
            args.getValue("transform", ValueTransformers::fromConfig)
    );

    @Override
    public double resolve(Entity entity) {
        double baseValue = CraftEngine.instance().attributeManager().vanillaAttributeDefaultBaseValue(entity, this.attribute, Double.NaN);
        if (Double.isNaN(baseValue)) return this.fallback;
        return this.transformer == null ? baseValue : this.transformer.transform(baseValue);
    }
}
