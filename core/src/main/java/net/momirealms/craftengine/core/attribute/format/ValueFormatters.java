package net.momirealms.craftengine.core.attribute.format;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.plugin.config.KnownResourceException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.Map;

public final class ValueFormatters {
    public static final ValueFormatterType<DecimalValueFormatter> DECIMAL = register(Key.ce("decimal"), DecimalValueFormatter.FACTORY);
    public static final ValueFormatterType<FixedValueFormatter> FIXED = register(Key.ce("fixed"), FixedValueFormatter.FACTORY);
    public static final ValueFormatterType<FixedValueFormatter> PERCENT = register(Key.ce("percent"), FixedValueFormatter.PERCENT_FACTORY);

    private ValueFormatters() {}

    public static <T extends ValueFormatter> ValueFormatterType<T> register(Key key, ValueFormatterFactory<T> factory) {
        ValueFormatterType<T> type = new ValueFormatterType<>(key, factory);
        ((WritableRegistry<ValueFormatterType<? extends ValueFormatter>>) BuiltInRegistries.VALUE_FORMATTER_TYPE)
                .register(ResourceKey.create(Registries.VALUE_FORMATTER_TYPE.location(), key), type);
        return type;
    }

    public static ValueFormatter fromConfig(ConfigValue value) {
        if (value.value() instanceof Map<?, ?>) {
            return fromConfig(value.getAsSection());
        }
        return DecimalValueFormatter.ofPattern(value.getAsString());
    }

    public static ValueFormatter fromConfig(ConfigSection section) {
        String type = section.getString("type", "decimal");
        Key key = Key.ce(type);
        ValueFormatterType<? extends ValueFormatter> formatterType = BuiltInRegistries.VALUE_FORMATTER_TYPE.getValue(key);
        if (formatterType == null) {
            throw new KnownResourceException("attribute.value_formatter.unknown_type", section.assemblePath("type"), type);
        }
        return formatterType.factory().create(section);
    }
}
