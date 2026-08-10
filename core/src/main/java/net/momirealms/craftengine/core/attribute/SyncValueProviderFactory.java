package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;

public interface SyncValueProviderFactory<T extends SyncValueProvider> {

    T create(ConfigSection args);
}
