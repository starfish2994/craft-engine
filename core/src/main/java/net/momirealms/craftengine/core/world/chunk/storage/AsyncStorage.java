package net.momirealms.craftengine.core.world.chunk.storage;

import ca.spottedleaf.concurrentutil.map.concurrent.longs.ConcurrentChainedLong2ReferenceHashTable;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.ChunkPos;
import net.momirealms.craftengine.core.world.WorldSettings;
import net.momirealms.craftengine.core.world.chunk.CEChunk;
import net.momirealms.craftengine.core.world.chunk.Chunk;
import net.momirealms.craftengine.core.world.chunk.serialization.DefaultChunkSerializer;
import net.momirealms.sparrow.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class AsyncStorage implements WorldDataStorage {
    private static final int MAX_PENDING_WRITES = 256;
    private static final ExecutorService WRITE_EXECUTOR;

    static {
        AtomicInteger threadCounter = new AtomicInteger();
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable, "craft-engine-chunk-io-" + threadCounter.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };
        WRITE_EXECUTOR = Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors() / 8), threadFactory);
    }

    private final RegionStorage storage;
    private final CompressionMethod compression;
    private final ConcurrentChainedLong2ReferenceHashTable<PendingWrite> pendingWrites = ConcurrentChainedLong2ReferenceHashTable.createWithCapacity(64);
    private final Object flushLock = new Object();

    public AsyncStorage(RegionStorage storage) {
        this.storage = storage;
        this.compression = CompressionMethod.fromId(Config.compressionMethod());
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
    public @NotNull CEChunk readChunkAt(@NotNull CEWorld world, @NotNull ChunkPos pos, @Nullable Chunk chunkAccess) throws IOException {
        PendingWrite pending = this.pendingWrites.get(pos.longKey);
        if (pending != null) {
            synchronized (pending) {
                if (!pending.completed) {
                    if (pending.tag == null) {
                        // 在途删除，等同于磁盘上不存在
                        return this.storage.chunkFactory().create(world, pos);
                    }
                    return DefaultChunkSerializer.deserialize(this.storage.chunkFactory(), world, pos, pending.tag);
                }
            }
        }
        return this.storage.readChunkAt(world, pos, chunkAccess);
    }

    @Override
    public @Nullable CompoundTag readChunkTagAt(@NotNull ChunkPos pos) throws IOException {
        PendingWrite pending = this.pendingWrites.get(pos.longKey);
        if (pending != null) {
            synchronized (pending) {
                if (!pending.completed) {
                    // 防御性拷贝，调用方可能修改返回的 tag
                    return pending.tag == null ? null : pending.tag.copy();
                }
            }
        }
        return this.storage.readChunkTagAt(pos);
    }

    @Override
    public void writeChunkAt(@NotNull ChunkPos pos, @NotNull CEChunk chunk) {
        // 序列化必须在调用线程上做，活区块数据不允许异步线程读取
        CompoundTag tag = DefaultChunkSerializer.serialize(chunk);
        submit(pos, tag, chunk);
    }

    @Override
    public void writeChunkTagAt(@NotNull ChunkPos pos, @Nullable CompoundTag nbt) {
        submit(pos, nbt, null);
    }

    @Override
    public void clearChunkAt(@NotNull ChunkPos pos) {
        submit(pos, null, null);
    }

    private void submit(ChunkPos pos, @Nullable CompoundTag tag, @Nullable CEChunk chunk) {
        long key = pos.longKey;
        PendingWrite pending;
        boolean schedule = false;
        while (true) {
            pending = this.pendingWrites.compute(key, (k, old) -> old == null || old.completed ? new PendingWrite(pos) : old);
            synchronized (pending) {
                if (pending.completed) {
                    // 与完成路径的摘除操作竞态，重试拿到一个全新的任务
                    continue;
                }
                pending.tag = tag;
                if (chunk != null) {
                    pending.chunk = chunk;
                }
                if (!pending.running) {
                    pending.running = true;
                    schedule = true;
                }
            }
            break;
        }
        if (schedule) {
            PendingWrite task = pending;
            WRITE_EXECUTOR.execute(() -> runWrite(key, task));
        }
        applyBackpressure();
    }

    private void runWrite(long key, PendingWrite pending) {
        CompoundTag tag;
        synchronized (pending) {
            tag = pending.tag;
        }
        try {
            if (tag == null) {
                this.storage.clearChunkAt(pending.pos);
            } else {
                byte[] data = RegionFile.encodeChunkData(this.compression, tag);
                this.storage.writeChunkDataAt(pending.pos, data);
            }
        } catch (Throwable t) {
            CEChunk chunk;
            synchronized (pending) {
                chunk = pending.chunk;
            }
            // 回置脏标记，让下一轮保存重试，避免静默丢数据
            if (chunk != null) {
                chunk.setUnsaved(true);
            }
            CraftEngine.instance().logger().warn("Failed to save chunk at " + pending.pos, t);
        }
        synchronized (pending) {
            if (pending.tag != tag) {
                // 写入期间来了新数据，带着最新数据重跑
                WRITE_EXECUTOR.execute(() -> runWrite(key, pending));
                return;
            }
            pending.running = false;
            pending.completed = true;
            this.pendingWrites.remove(key, pending);
        }
        synchronized (this.flushLock) {
            this.flushLock.notifyAll();
        }
    }

    private void applyBackpressure() {
        if (this.pendingWrites.size() < MAX_PENDING_WRITES) return;
        long start = System.currentTimeMillis();
        boolean warned = false;
        while (this.pendingWrites.size() >= MAX_PENDING_WRITES) {
            synchronized (this.flushLock) {
                try {
                    this.flushLock.wait(50L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            if (!warned && System.currentTimeMillis() - start > 10_000L) {
                warned = true;
                CraftEngine.instance().logger().warn("Chunk IO executor is falling behind, pending writes: " + this.pendingWrites.size());
            }
        }
    }

    private void awaitPendingWrites() {
        while (!this.pendingWrites.isEmpty()) {
            synchronized (this.flushLock) {
                if (this.pendingWrites.isEmpty()) break;
                try {
                    this.flushLock.wait(100L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    @Override
    public void flush() throws IOException {
        this.awaitPendingWrites();
        this.storage.flush();
    }

    @Override
    public void close() throws IOException {
        this.awaitPendingWrites();
        this.storage.close();
    }

    private static final class PendingWrite {
        final ChunkPos pos;
        @Nullable CompoundTag tag;  // null 表示删除；仅在 synchronized (this) 下访问
        @Nullable CEChunk chunk;    // 用于写失败时回置脏标记
        boolean running;
        boolean completed;

        PendingWrite(ChunkPos pos) {
            this.pos = pos;
        }
    }
}
