package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.util.Key;

public record BaseValueSourceType<T extends BaseValueSource>(Key id, BaseValueSourceFactory<T> factory) {
}
