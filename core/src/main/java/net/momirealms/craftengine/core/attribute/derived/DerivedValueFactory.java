package net.momirealms.craftengine.core.attribute.derived;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;

public interface DerivedValueFactory<T extends DerivedValue> {

    T create(ConfigSection args);
}
