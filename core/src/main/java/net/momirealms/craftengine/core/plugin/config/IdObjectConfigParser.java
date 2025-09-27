package net.momirealms.craftengine.core.plugin.config;

import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.locale.LocalizedException;
import net.momirealms.craftengine.core.util.Key;

import java.nio.file.Path;

public interface IdObjectConfigParser extends ConfigParser {

    default void parseObject(Pack pack, Path path, String node, Key id, Object object) throws LocalizedException {
    }
}
