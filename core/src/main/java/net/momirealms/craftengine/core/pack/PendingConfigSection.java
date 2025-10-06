package net.momirealms.craftengine.core.pack;

import net.momirealms.craftengine.core.util.Key;

import java.nio.file.Path;
import java.util.Map;

public record PendingConfigSection(Pack pack, Path path, String node, Key id, Map<String, Object> config) {
}
