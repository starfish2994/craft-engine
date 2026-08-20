package net.momirealms.craftengine.core.attribute.transform;

import net.momirealms.craftengine.core.util.Key;

public record ValueTransformerType<T extends ValueTransformer>(Key id, ValueTransformerFactory<T> factory) {
}
