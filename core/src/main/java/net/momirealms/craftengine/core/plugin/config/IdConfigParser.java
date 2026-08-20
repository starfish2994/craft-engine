package net.momirealms.craftengine.core.plugin.config;

import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class IdConfigParser extends AbstractConfigParser {
    protected final Map<Key, Path> idToPath;

    protected IdConfigParser() {
        this.idToPath = this.checkDuplicated() ? (async() ? new ConcurrentHashMap<>(128, 0.5f) : new HashMap<>(128, 0.5f)) : Map.of();
    }

    protected boolean checkDuplicated() {
        return true;
    }

    protected boolean isDuplicate(final Key id, final Path filePath, String currentNode) {
        Path duplicatedPath = this.idToPath.put(id, filePath);
        if (duplicatedPath != null) {
            error(new KnownResourceException(filePath, "resource.duplicated_id", currentNode, duplicatedPath.toString()));
            return true;
        }
        return false;
    }

    public boolean supportSearch() {
        return true;
    }

    @Nullable
    public Path pathById(Key id) {
        return this.idToPath.get(id);
    }

    @NotNull
    public Collection<Key> registeredKeys() {
        return this.idToPath.keySet();
    }

    @Override
    public void clearConfigs() {
        super.clearConfigs();
        if (!this.supportSearch()) {
            clearIdToPath();
        }
    }

    public void clearIdToPath() {
        if (!this.idToPath.isEmpty()) {
            this.idToPath.clear();
        }
    }
}
