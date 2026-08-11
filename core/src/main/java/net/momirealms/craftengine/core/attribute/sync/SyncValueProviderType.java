package net.momirealms.craftengine.core.attribute.sync;

import net.momirealms.craftengine.core.util.Key;

public record SyncValueProviderType<T extends SyncValueProvider>(Key id, SyncValueProviderFactory<T> factory) {
}
