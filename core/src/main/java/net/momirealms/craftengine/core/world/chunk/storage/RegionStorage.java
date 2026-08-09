package net.momirealms.craftengine.core.world.chunk.storage;

import net.momirealms.craftengine.core.util.FileUtils;
import net.momirealms.craftengine.core.world.ChunkPos;
import net.momirealms.craftengine.core.world.WorldSettings;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.NBT;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A region-file based storage that accepts pre-encoded chunk data, allowing
 * {@link AsyncStorage} to offload encoding and writing to its I/O executor.
 */
public interface RegionStorage extends WorldDataStorage {

    String WORLD_SETTINGS_FILE = "craftengine_settings.dat";

    ChunkFactory chunkFactory();

    Path folder();

    void writeChunkDataAt(@NotNull ChunkPos pos, byte[] data) throws IOException;

    @Override
    default WorldSettings readSettings() throws IOException {
        Path resolve = this.folder().getParent().resolve(WORLD_SETTINGS_FILE);
        if (!Files.exists(resolve)) {
            return new WorldSettings();
        }
        CompoundTag tag = NBT.readFile(resolve);
        if (tag == null) {
            return new WorldSettings();
        }
        return new WorldSettings(tag);
    }

    @Override
    default void writeSettings(WorldSettings settings) throws IOException {
        Path parent = this.folder().getParent();
        Path resolve = parent.resolve(WORLD_SETTINGS_FILE);
        FileUtils.createDirectoriesSafe(parent);
        NBT.writeFile(resolve, settings.tag());
    }
}
