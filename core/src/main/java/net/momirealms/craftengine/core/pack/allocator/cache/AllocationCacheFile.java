package net.momirealms.craftengine.core.pack.allocator.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

public class AllocationCacheFile<T extends Comparable<T>, A> {
    private final Map<String, T> cache;
    private final CacheStorage<A> cacheStorage;
    private final CacheSerializer<T, A> serializer;

    public AllocationCacheFile(CacheStorage<A> cacheStorage, CacheSerializer<T, A> serializer) {
        this.cache = new HashMap<>();
        this.cacheStorage = cacheStorage;
        this.serializer = serializer;
    }

    public Map<String, T> cache() {
        return this.cache;
    }

    public void clear() {
        this.cache.clear();
    }

    public CompletableFuture<Void> load() {
        return this.cacheStorage.load().thenAccept(a -> {
            Map<String, T> deserialized = this.serializer.deserialize(a);
            this.cache.putAll(deserialized);
        });
    }

    public CompletableFuture<Void> save() {
        Map<T, String> sortedById = new TreeMap<>();
        for (Map.Entry<String, T> entry : this.cache.entrySet()) {
            sortedById.put(entry.getValue(), entry.getKey());
        }
        return this.cacheStorage.save(this.serializer.serialize(sortedById));
    }

    public Iterable<Map.Entry<String, T>> entrySet() {
        return this.cache.entrySet();
    }

    public Set<String> keySet() {
        return this.cache.keySet();
    }

    public void put(String name, T newId) {
        this.cache.put(name, newId);
    }

    public T remove(String name) {
        return this.cache.remove(name);
    }

    public T get(String name) {
        return this.cache.get(name);
    }
}
