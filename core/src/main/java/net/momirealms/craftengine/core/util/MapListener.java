package net.momirealms.craftengine.core.util;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;

public class MapListener<K, V> implements Map<K, V> {

    private final Map<K, V> delegateMap;
    private final List<BiConsumer<K, V>> putListeners = new ArrayList<>();
    private final List<BiConsumer<K, V>> removeListeners = new ArrayList<>();

    public MapListener(Map<K, V> original) {
        this.delegateMap = original;
    }

    public void registerPutListener(BiConsumer<K, V> listener) {
        this.putListeners.add(listener);
    }

    public void registerRemoveListener(BiConsumer<K, V> listener) {
        this.removeListeners.add(listener);
    }

    @Override
    public int size() {
        return this.delegateMap.size();
    }

    @Override
    public boolean isEmpty() {
        return this.delegateMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.delegateMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.delegateMap.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return this.delegateMap.get(key);
    }

    @Override
    public @Nullable V put(K key, V value) {
        for (BiConsumer<K, V> listener : this.putListeners) {
            listener.accept(key, value);
        }
        return this.delegateMap.put(key, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V remove(Object key) {
        V removed = this.delegateMap.remove(key);
        if (removed != null) {
            for (BiConsumer<K, V> listener : this.removeListeners) {
                listener.accept((K) key, removed);
            }
        }
        return removed;
    }

    @Override
    public void putAll(@NonNull Map<? extends K, ? extends V> m) {
        this.delegateMap.putAll(m);
    }

    @Override
    public void clear() {
        this.delegateMap.clear();
    }

    @Override
    public @NonNull Set<K> keySet() {
        return this.delegateMap.keySet();
    }

    @Override
    public @NonNull Collection<V> values() {
        return this.delegateMap.values();
    }

    @Override
    public @NonNull Set<Entry<K, V>> entrySet() {
        return this.delegateMap.entrySet();
    }
}
