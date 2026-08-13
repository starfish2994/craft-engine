package net.momirealms.craftengine.core.attribute.derived;

import net.momirealms.craftengine.core.util.Key;

public record DerivedValueType<T extends DerivedValue>(Key id, DerivedValueFactory<T> factory) {
}
