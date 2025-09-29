package net.momirealms.craftengine.core.plugin.config;

import net.momirealms.craftengine.core.pack.CachedConfigSection;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.locale.LocalizedException;
import net.momirealms.craftengine.core.util.GsonHelper;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.nio.file.Path;
import java.util.Map;

public abstract class SectionConfigParser extends AbstractConfigParser {

    @Override
    protected void parseSection(CachedConfigSection cached) {
        ResourceConfigUtils.runCatching(
                cached.filePath(),
                cached.prefix(),
                () -> parseSection(cached.pack(), cached.filePath(), cached.config()),
                () -> GsonHelper.get().toJson(cached.config())
        );
    }

    protected abstract void parseSection(Pack pack, Path path, Map<String, Object> section) throws LocalizedException;
}
