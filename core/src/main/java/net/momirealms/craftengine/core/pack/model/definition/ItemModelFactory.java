package net.momirealms.craftengine.core.pack.model.definition;

import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;

import java.nio.file.Path;

@FunctionalInterface
public interface ItemModelFactory<T extends ItemModel> {

    T create(Pack pack, Path path, ConfigSection section);
}
