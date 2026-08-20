package net.momirealms.craftengine.core.world.chunk.storage;

import net.momirealms.craftengine.core.util.FileUtils;
import net.momirealms.craftengine.core.world.ChunkPos;
import net.momirealms.craftengine.core.world.WorldSettings;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.NBT;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    /**
     * Parses the region coordinates from a region file name ("r.&lt;x&gt;.&lt;z&gt;&lt;suffix&gt;").
     *
     * @return The chunk position of the region's origin, or null if the file name doesn't match.
     */
    @Nullable
    static ChunkPos parseRegionFileCoordinates(Path file, String prefix, String suffix) {
        String fileName = file.getFileName().toString();
        if (!fileName.startsWith(prefix) || !fileName.endsWith(suffix)) {
            return null;
        }
        int separator = fileName.indexOf('.', prefix.length());
        if (separator < 0) {
            return null;
        }
        int suffixStart = fileName.length() - suffix.length();
        try {
            int x = Integer.parseInt(fileName.substring(prefix.length(), separator));
            int z = Integer.parseInt(fileName.substring(separator + 1, suffixStart));
            return new ChunkPos(x << 5, z << 5);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
