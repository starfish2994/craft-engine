package net.momirealms.craftengine.core.attribute.format;

import net.momirealms.craftengine.core.util.Key;

public record ValueFormatterType<T extends ValueFormatter>(Key id, ValueFormatterFactory<T> factory) {
}
