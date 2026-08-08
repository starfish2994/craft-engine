package net.momirealms.craftengine.core.util;

import ca.spottedleaf.concurrentutil.collection.iterator.BaseObjectIterator;
import ca.spottedleaf.concurrentutil.map.concurrent.longs.ConcurrentChainedLong2ReferenceHashTable;
import net.momirealms.craftengine.core.plugin.CraftEngine;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

// 比 caffeine 的快1倍
public final class ExpiringLong2ObjectCache<V> {
    private static final long IDLE = Long.MAX_VALUE;
    private static final long MAX_TOLERANCE_NANOS = TimeUnit.SECONDS.toNanos(1);
    private final ConcurrentChainedLong2ReferenceHashTable<Entry<V>> table;
    private final long expireAfterAccessNanos;
    private final long accessRefreshThresholdNanos;
    private final long pacerToleranceNanos;
    private final AtomicLong nextFireTimeNanos = new AtomicLong(IDLE);
    private final AtomicLong sweepGeneration = new AtomicLong();

    public ExpiringLong2ObjectCache(long expireAfterAccess, TimeUnit unit) {
        this(expireAfterAccess, unit, 0);
    }

    public ExpiringLong2ObjectCache(long expireAfterAccess, TimeUnit unit, int expectedSize) {
        this.expireAfterAccessNanos = unit.toNanos(expireAfterAccess);
        this.accessRefreshThresholdNanos = Math.max(1, this.expireAfterAccessNanos >> 2);
        this.pacerToleranceNanos = Math.min(MAX_TOLERANCE_NANOS, this.accessRefreshThresholdNanos);
        this.table = expectedSize > 0
                ? ConcurrentChainedLong2ReferenceHashTable.createWithExpected(expectedSize)
                : new ConcurrentChainedLong2ReferenceHashTable<>();
    }

    public V getIfPresent(long key) {
        Entry<V> entry = this.table.get(key);
        if (entry == null) {
            return null;
        }
        long now = System.nanoTime();
        long lastAccess = entry.lastAccessNanos;
        if (now - lastAccess > this.expireAfterAccessNanos) {
            this.table.remove(key, entry);
            return null;
        }
        // 时间戳按 TTL/4 的粒度粗化刷新，避免高并发读同一 key 时 volatile 写导致缓存行乒乓
        if (now - lastAccess > this.accessRefreshThresholdNanos) {
            entry.lastAccessNanos = now;
        }
        return entry.value;
    }

    public void put(long key, V value) {
        this.table.put(key, new Entry<>(value, System.nanoTime()));
        scheduleSweepAt(System.nanoTime() + this.expireAfterAccessNanos);
    }

    public void invalidate(long key) {
        this.table.remove(key);
    }

    private void scheduleSweepAt(long fireTime) {
        while (true) {
            long current = this.nextFireTimeNanos.get();
            if (current <= fireTime + this.pacerToleranceNanos) {
                return;
            }
            if (this.nextFireTimeNanos.compareAndSet(current, fireTime)) {
                arm(fireTime);
                return;
            }
        }
    }

    private void arm(long fireTime) {
        long generation = this.sweepGeneration.incrementAndGet();
        long delay = Math.max(fireTime - System.nanoTime(), this.pacerToleranceNanos);
        CompletableFuture.runAsync(() -> fire(generation, fireTime), CompletableFuture.delayedExecutor(delay, TimeUnit.NANOSECONDS, CraftEngine.instance().scheduler().async()));
    }

    private void fire(long generation, long myFireTime) {
        if (this.sweepGeneration.get() != generation) {
            return; // 已被更早的重排取代
        }
        long nextExpiry = sweep(System.nanoTime());
        // 按剩余条目的最早过期时间重排；表空则停排
        while (true) {
            if (this.sweepGeneration.get() != generation) {
                return; // sweep 期间出现了更早的调度，让位
            }
            // 触发时刻被改动说明已有并发调度接管——即便其 generation 自增尚未可见，它也必定会 arm，
            // 此时必须让位而不能抢写，否则双方都会认为对方会续排，导致清扫链断裂
            if (this.nextFireTimeNanos.get() != myFireTime) {
                return;
            }
            if (this.nextFireTimeNanos.compareAndSet(myFireTime, nextExpiry)) {
                if (nextExpiry != IDLE) {
                    arm(nextExpiry);
                }
                return;
            }
        }
    }

    private long sweep(long now) {
        long minExpiry = IDLE;
        BaseObjectIterator<ConcurrentChainedLong2ReferenceHashTable.TableEntry<Entry<V>>> iterator = this.table.entryIterator();
        while (iterator.hasNext()) {
            ConcurrentChainedLong2ReferenceHashTable.TableEntry<Entry<V>> tableEntry = iterator.next();
            Entry<V> entry = tableEntry.getValue();
            long expiry = entry.lastAccessNanos + this.expireAfterAccessNanos;
            if (now - entry.lastAccessNanos > this.expireAfterAccessNanos) {
                this.table.remove(tableEntry.getKey(), entry);
            } else if (expiry < minExpiry) {
                minExpiry = expiry;
            }
        }
        return minExpiry;
    }

    private static final class Entry<V> {
        final V value;
        volatile long lastAccessNanos;

        Entry(V value, long lastAccessNanos) {
            this.value = value;
            this.lastAccessNanos = lastAccessNanos;
        }
    }
}
