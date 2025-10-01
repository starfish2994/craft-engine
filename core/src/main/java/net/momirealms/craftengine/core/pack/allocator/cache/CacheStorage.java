package net.momirealms.craftengine.core.pack.allocator.cache;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class CacheStorage<A> {
    private final CacheFileStorage<A> storage;
    private A lastReadValue;

    public CacheStorage(CacheFileStorage<A> storage) {
        this.storage = storage;
    }

    public CompletableFuture<Void> save(@NotNull final A value) {
        if (!value.equals(this.lastReadValue) || this.storage.needForceUpdate()) {
            this.lastReadValue = value;
            return this.storage.save(value);
        }
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<A> load() {
        if (this.lastReadValue != null && !this.storage.needForceUpdate()) {
            return CompletableFuture.completedFuture(this.lastReadValue);
        }
        return this.storage.load().thenApply(a -> {
            this.lastReadValue = a;
            return a;
        });
    }
}
