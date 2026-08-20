package net.momirealms.craftengine.core.attribute.base;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.plugin.config.KnownResourceException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.Map;

public final class BaseValueSources {
    public static final BaseValueSourceType<ConstantBaseValueSource> CONSTANT = register(Key.ce("constant"), ConstantBaseValueSource.FACTORY);
    public static final BaseValueSourceType<VanillaBaseValueSource> VANILLA = register(Key.ce("vanilla"), VanillaBaseValueSource.FACTORY);
    public static final BaseValueSourceType<ByEntityTypeBaseValueSource> BY_ENTITY_TYPE = register(Key.ce("by_entity_type"), ByEntityTypeBaseValueSource.FACTORY);

    private BaseValueSources() {}

    public static <T extends BaseValueSource> BaseValueSourceType<T> register(Key key, BaseValueSourceFactory<T> factory) {
        BaseValueSourceType<T> type = new BaseValueSourceType<>(key, factory);
        ((WritableRegistry<BaseValueSourceType<? extends BaseValueSource>>) BuiltInRegistries.BASE_VALUE_SOURCE_TYPE)
                .register(ResourceKey.create(Registries.BASE_VALUE_SOURCE_TYPE.location(), key), type);
        return type;
    }

    public static BaseValueSource fromConfig(ConfigValue value) {
        if (value.is(Map.class)) {
            return fromConfig(value.getAsSection());
        }
        return new ConstantBaseValueSource(value.getAsDouble());
    }

    public static BaseValueSource fromConfig(ConfigSection section) {
        String type = section.getNonNullString("type");
        Key key = Key.ce(type);
        BaseValueSourceType<? extends BaseValueSource> sourceType = BuiltInRegistries.BASE_VALUE_SOURCE_TYPE.getValue(key);
        if (sourceType == null) {
            throw new KnownResourceException("attribute.base_value_source.unknown_type", section.assemblePath("type"), type);
        }
        return sourceType.factory().create(section);
    }
}
