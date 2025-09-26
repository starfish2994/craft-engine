package net.momirealms.craftengine.core.plugin.config;

import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.locale.LocalizedException;

import java.nio.file.Path;
import java.util.Map;

public interface SectionConfigParser extends ConfigParser {

    default void parseSection(Pack pack, Path path, Map<String, Object> section) throws LocalizedException {
    }
}
