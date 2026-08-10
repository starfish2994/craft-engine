package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;

public interface ValueTransformerFactory<T extends ValueTransformer> {

    T create(ConfigSection args);
}
