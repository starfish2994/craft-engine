package net.momirealms.craftengine.core.attribute.format;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;

public interface ValueFormatterFactory<T extends ValueFormatter> {

    T create(ConfigSection args);
}
