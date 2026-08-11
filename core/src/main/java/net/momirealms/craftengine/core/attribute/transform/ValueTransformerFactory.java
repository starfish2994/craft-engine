package net.momirealms.craftengine.core.attribute.transform;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;

public interface ValueTransformerFactory<T extends ValueTransformer> {

    T create(ConfigSection args);
}
