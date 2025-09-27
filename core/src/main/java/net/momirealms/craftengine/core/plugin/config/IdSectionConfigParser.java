package net.momirealms.craftengine.core.plugin.config;

import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.locale.LocalizedException;
import net.momirealms.craftengine.core.util.Key;

import java.nio.file.Path;
import java.util.Map;

public interface IdSectionConfigParser extends ConfigParser {

    default void parseSection(Pack pack, Path path, String node, Key id, Map<String, Object> section) throws LocalizedException {
    }
}
