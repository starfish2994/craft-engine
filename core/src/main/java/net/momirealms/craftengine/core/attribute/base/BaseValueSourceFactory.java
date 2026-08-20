package net.momirealms.craftengine.core.attribute.base;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;

public interface BaseValueSourceFactory<T extends BaseValueSource> {

    T create(ConfigSection args);
}
