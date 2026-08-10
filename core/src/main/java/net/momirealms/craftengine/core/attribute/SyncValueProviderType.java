package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.util.Key;

public record SyncValueProviderType<T extends SyncValueProvider>(Key id, SyncValueProviderFactory<T> factory) {
}
