package net.momirealms.craftengine.core.attribute.damage;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.KnownResourceException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;

public final class DamageIndicators {

    private DamageIndicators() {}

    public static <T extends DamageIndicator> DamageIndicatorType<T> register(Key key, DamageIndicatorFactory<T> factory) {
        DamageIndicatorType<T> type = new DamageIndicatorType<>(key, factory);
        ((WritableRegistry<DamageIndicatorType<? extends DamageIndicator>>) BuiltInRegistries.DAMAGE_INDICATOR_TYPE)
                .register(ResourceKey.create(Registries.DAMAGE_INDICATOR_TYPE.location(), key), type);
        return type;
    }

    public static DamageIndicator fromConfig(ConfigSection section) {
        String type = section.getString("type", "text");
        Key key = Key.ce(type);
        DamageIndicatorType<? extends DamageIndicator> indicatorType = BuiltInRegistries.DAMAGE_INDICATOR_TYPE.getValue(key);
        if (indicatorType == null) {
            throw new KnownResourceException("damage_indicator.unknown_type", section.assemblePath("type"), type);
        }
        return indicatorType.factory().create(section);
    }
}
