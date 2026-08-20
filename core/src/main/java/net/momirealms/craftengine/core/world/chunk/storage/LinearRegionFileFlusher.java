package net.momirealms.craftengine.core.world.chunk.storage;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.momirealms.craftengine.core.plugin.CraftEngine;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Ported from Luminol <a href="https://github.com/LuminolMC/Luminol">...</a>
 * The implementation follows the upstream code as closely as possible. Deliberate divergences,
 * limited to CraftEngine adaptations:
 */
public final class LinearRegionFileFlusher implements Runnable {
    private static final class Holder {
        private static final LinearRegionFileFlusher INSTANCE = new LinearRegionFileFlusher(
                Math.max(1, Runtime.getRuntime().availableProcessors() / 8),
                1000L,
                5000L
        );
    }

    public static LinearRegionFileFlusher shared() {
        return Holder.INSTANCE;
    }

    private final Set<BufferedLinearRegionFile> inManagement = new ObjectArraySet<>();
    private final ScheduledFuture<?> flusherChecker;
    private final ExecutorService ioWorkerPool;
    private final long flushOfWriteTimeoutMs;

    public LinearRegionFileFlusher(int nIoThreads, long checkIntervalMs, long flushOfWriteTimeoutMs) {
        if (nIoThreads <= 0) throw new IllegalArgumentException("Number of I/O threads must > 0!");
        if (checkIntervalMs <= 0) throw new IllegalArgumentException("Check interval must > 0");
        if (flushOfWriteTimeoutMs <= 0) throw new IllegalArgumentException("Flush of write timeout must > 0");

        this.ioWorkerPool = Executors.newFixedThreadPool(nIoThreads, new ThreadFactoryBuilder()
                .setNameFormat("craft-engine-linear-region-worker-%d")
                .setDaemon(true)
                .build()
        );
        this.flusherChecker = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                        .setNameFormat("craft-engine-linear-region-checker")
                        .setDaemon(true)
                        .build())
                .scheduleWithFixedDelay(this, checkIntervalMs, checkIntervalMs, TimeUnit.MILLISECONDS);
        this.flushOfWriteTimeoutMs = flushOfWriteTimeoutMs;
    }

    public void shutdown() {
        this.flusherChecker.cancel(false);

        this.ioWorkerPool.shutdown();
        for (; ; ) {
            try {
                if (this.ioWorkerPool.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                    break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void run() {
        final long currentNanos = System.nanoTime();
        final BufferedLinearRegionFile[] copied;

        synchronized (this) {
            copied = this.inManagement.toArray(new BufferedLinearRegionFile[0]);
        }

        final List<BufferedLinearRegionFile> toRemove = new ObjectArrayList<>();
        for (BufferedLinearRegionFile file : copied) {
            // try acquiring the read lock
            if (!file.softReadLock()) {
                // if the read lock is unacquirable, it might mean there is another operation processing(might be a writing operation)
                continue;
            }

            boolean closed;

            try {
                // check if the file is closed
                closed = file.isClosedRaw();
            } finally {
                file.releaseReadLock();
            }

            if (closed) {
                // add to pending remove list so that we could clean the closed file correctly
                toRemove.add(file);
                continue;
            }

            // skip non sync-required files
            if (!file.shouldSync()) {
                continue;
            }

            final long lastWriteNanos = file.getLastWritten();
            final long timeElapsed = (currentNanos - lastWriteNanos) / 1_000_000; // Convert to milliseconds

            // if deadline(timeout) reached
            if (timeElapsed >= this.flushOfWriteTimeoutMs) {
                // already marked to flush
                if (!file.markAsBeingSynced()) {
                    continue;
                }

                this.ioWorkerPool.execute(() -> {
                    try {
                        file.syncIfNeeded();
                    } catch (IOException e) {
                        CraftEngine.instance().logger().warn("Failed to sync master file: ", e);
                    }
                });
            }
        }

        synchronized (this) {
            // clean closed files
            for (BufferedLinearRegionFile file : toRemove) {
                this.inManagement.remove(file);
            }
        }
    }

    public void removeFile(BufferedLinearRegionFile fileToRemove) {
        synchronized (this) {
            this.inManagement.remove(fileToRemove);
        }
    }

    public void addFile(BufferedLinearRegionFile fileToAdd) {
        synchronized (this) {
            this.inManagement.add(fileToAdd);
        }
    }
}
