package net.momirealms.craftengine.bukkit.world.chunk.storage;

import net.momirealms.craftengine.bukkit.world.FoliaCEChunk;
import net.momirealms.craftengine.bukkit.world.chunk.serialization.FoliaChunkSerializer;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.ChunkPos;
import net.momirealms.craftengine.core.world.chunk.CEChunk;
import net.momirealms.craftengine.core.world.chunk.serialization.DefaultChunkSerializer;
import net.momirealms.craftengine.core.world.chunk.storage.DefaultRegionFileStorage;
import net.momirealms.craftengine.core.world.chunk.storage.RegionFile;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.NBT;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Path;

public final class FoliaRegionFileStorage extends DefaultRegionFileStorage {

    public FoliaRegionFileStorage(Path directory) {
        super(directory);
    }

    @Override
    public CEChunk readNewChunkAt(CEWorld world, ChunkPos pos) throws IOException {
        RegionFile regionFile = this.getRegionFile(pos, false);
        synchronized (regionFile) {
            if (regionFile.hasChunk(pos)) {
                regionFile.clear(pos);
            }
            return new FoliaCEChunk(world, pos);
        }
    }

    @Override
    public @NotNull CEChunk readChunkAt(@NotNull CEWorld world, @NotNull ChunkPos pos) throws IOException {
        RegionFile regionFile = this.getRegionFile(pos, true);
        if (regionFile == null) {
            return new FoliaCEChunk(world, pos);
        }

        synchronized (regionFile) {
            try (DataInputStream dataInputStream = regionFile.getChunkDataInputStream(pos)) {
                if (dataInputStream == null) {
                    return new FoliaCEChunk(world, pos);
                }
                CompoundTag tag = NBT.readCompound(dataInputStream, false);
                return DefaultChunkSerializer.deserialize(world, pos, tag);
            }
        }
    }
}
