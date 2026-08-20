package net.momirealms.craftengine.core.world.chunk.storage;

import net.momirealms.craftengine.core.util.ExpiringLong2ObjectCache;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.ChunkPos;
import net.momirealms.craftengine.core.world.WorldSettings;
import net.momirealms.craftengine.core.world.chunk.CEChunk;
import net.momirealms.craftengine.core.world.chunk.Chunk;
import net.momirealms.sparrow.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public final class CachedStorage<T extends WorldDataStorage> implements WorldDataStorage {
    private final T storage;
    private final ExpiringLong2ObjectCache<CEChunk> chunkCache;

    public CachedStorage(T storage) {
        this.storage = storage;
        this.chunkCache = new ExpiringLong2ObjectCache<>(30, TimeUnit.SECONDS, 4096);
    }

    @Override
    public WorldSettings readSettings() throws IOException {
        return this.storage.readSettings();
    }

    @Override
    public void writeSettings(WorldSettings settings) throws IOException {
        this.storage.writeSettings(settings);
    }

    @Override
    public CEChunk readNewChunkAt(CEWorld world, ChunkPos pos) throws IOException {
        return this.storage.readNewChunkAt(world, pos);
    }

    @Override
    public @NotNull CEChunk readChunkAt(@NotNull CEWorld world, @NotNull ChunkPos pos, @Nullable Chunk chunkAccess) throws IOException {
        CEChunk chunk = this.chunkCache.getIfPresent(pos.longKey);
        if (chunk != null) {
            return chunk;
        }
        chunk = this.storage.readChunkAt(world, pos, chunkAccess);
        this.chunkCache.put(pos.longKey, chunk);
        return chunk;
    }

    @Override
    public void preloadChunkAt(@NotNull CEWorld world, @NotNull ChunkPos pos, @Nullable Chunk chunkAccess) throws IOException {
        if (this.chunkCache.getIfPresent(pos.longKey) == null) {
            this.chunkCache.put(pos.longKey, this.storage.readChunkAt(world, pos, chunkAccess));
        }
    }

    @Override
    public void writeChunkAt(@NotNull ChunkPos pos, @NotNull CEChunk chunk) throws IOException {
        this.storage.writeChunkAt(pos, chunk);
    }

    @Override
    public @Nullable CompoundTag readChunkTagAt(@NotNull ChunkPos pos) throws IOException {
        return this.storage.readChunkTagAt(pos);
    }

    @Override
    public void writeChunkTagAt(@NotNull ChunkPos pos, @Nullable CompoundTag nbt) throws IOException {
        this.storage.writeChunkTagAt(pos, nbt);
    }

    @Override
    public void clearChunkAt(@NotNull ChunkPos pos) throws IOException {
        this.chunkCache.invalidate(pos.longKey);
        this.storage.clearChunkAt(pos);
    }

    @Override
    public void close() throws IOException {
        this.storage.close();
    }

    @Override
    public void flush() throws IOException {
        this.storage.flush();
    }
}
