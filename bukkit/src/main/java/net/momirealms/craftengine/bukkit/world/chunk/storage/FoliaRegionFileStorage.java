package net.momirealms.craftengine.bukkit.world.chunk.storage;

import net.momirealms.craftengine.bukkit.world.FoliaCEChunk;
import net.momirealms.craftengine.bukkit.world.chunk.serialization.FoliaChunkSerializer;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.ChunkPos;
import net.momirealms.craftengine.core.world.chunk.CEChunk;
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
        RegionFile regionFile = this.getRegionFile(pos, false, true);
        try {
            if (regionFile.doesChunkExist(pos)) {
                regionFile.clear(pos);
            }
            return new FoliaCEChunk(world, pos);
        } finally {
            regionFile.fileLock.unlock();
        }
    }

    @Override
    public @NotNull CEChunk readChunkAt(@NotNull CEWorld world, @NotNull ChunkPos pos) throws IOException {
        RegionFile regionFile = this.getRegionFile(pos, false, true);
        try {
            DataInputStream dataInputStream = regionFile.getChunkDataInputStream(pos);
            CompoundTag tag;
            try {
                if (dataInputStream == null) {
                    return new FoliaCEChunk(world, pos);
                }
                tag = NBT.readCompound(dataInputStream, false);
            } catch (Throwable t1) {
                try {
                    dataInputStream.close();
                } catch (Throwable t2) {
                    t1.addSuppressed(t2);
                }
                throw t1;
            }
            dataInputStream.close();
            return FoliaChunkSerializer.deserialize(world, pos, tag);
        } finally {
            regionFile.fileLock.unlock();
        }
    }
}
