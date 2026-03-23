package net.momirealms.craftengine.bukkit.world;

import net.momirealms.craftengine.bukkit.world.chunk.storage.FoliaRegionFileStorage;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.chunk.storage.*;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class BukkitStorageAdaptor implements StorageAdaptor {

    @Override
    public @NotNull WorldDataStorage adapt(@NotNull World world) {
        if (Config.chunkStorageType() == StorageType.NONE) {
            return new NoneStorage();
        }
        Path path = world.directory().resolve(CEWorld.REGION_DIRECTORY);
        if (Config.enableChunkCache()) {
            return new CachedStorage<>(VersionHelper.isFolia() ? new FoliaRegionFileStorage(path) : new DefaultRegionFileStorage(path));
        } else {
            return VersionHelper.isFolia() ? new FoliaRegionFileStorage(path) : new DefaultRegionFileStorage(path);
        }
    }
}
