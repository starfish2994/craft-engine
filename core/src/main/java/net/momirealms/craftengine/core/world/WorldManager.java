package net.momirealms.craftengine.core.world;

import net.momirealms.craftengine.core.plugin.Manageable;
import net.momirealms.craftengine.core.plugin.config.ConfigParser;
import net.momirealms.craftengine.core.world.chunk.storage.StorageAdaptor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface WorldManager extends Manageable {

    void setStorageAdaptor(@NotNull StorageAdaptor storageAdaptor);

    @NotNull
    StorageAdaptor getStorageAdaptor();

    World getWorldOffMainThread(UUID uuid);

    World getWorld(UUID uuid);

    ConfigParser[] parsers();
}
