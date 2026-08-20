package net.momirealms.craftengine.core.attribute.derived;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.plugin.config.KnownResourceException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.Map;

public final class DerivedValues {
    public static final DerivedValueType<ExpressionDerivedValue> EXPRESSION = register(Key.ce("expression"), ExpressionDerivedValue.FACTORY);

    private DerivedValues() {}

    public static <T extends DerivedValue> DerivedValueType<T> register(Key key, DerivedValueFactory<T> factory) {
        DerivedValueType<T> type = new DerivedValueType<>(key, factory);
        ((WritableRegistry<DerivedValueType<? extends DerivedValue>>) BuiltInRegistries.DERIVED_VALUE_TYPE)
                .register(ResourceKey.create(Registries.DERIVED_VALUE_TYPE.location(), key), type);
        return type;
    }

    public static DerivedValue fromConfig(ConfigValue value) {
        if (value.is(Map.class)) {
            return fromConfig(value.getAsSection());
        }
        return ExpressionDerivedValue.compile(value.path(), value.getAsString());
    }

    public static DerivedValue fromConfig(ConfigSection section) {
        String type = section.getString("type", "expression");
        Key key = Key.ce(type);
        DerivedValueType<? extends DerivedValue> derivedType = BuiltInRegistries.DERIVED_VALUE_TYPE.getValue(key);
        if (derivedType == null) {
            throw new KnownResourceException("attribute.derived.unknown_type", section.assemblePath("type"), type);
        }
        return derivedType.factory().create(section);
    }
}
