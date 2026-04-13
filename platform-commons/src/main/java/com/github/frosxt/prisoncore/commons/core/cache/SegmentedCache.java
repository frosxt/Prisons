package com.github.frosxt.prisoncore.commons.core.cache;

import com.github.frosxt.prisoncore.commons.api.cache.Cache;
import com.github.frosxt.prisoncore.commons.api.cache.CacheConfig;
import com.github.frosxt.prisoncore.commons.api.cache.CacheStats;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public final class SegmentedCache<K, V> implements Cache<K, V> {
    private static final int SEGMENT_COUNT = 16;

    private final Segment<K, V>[] segments;
    private final Duration ttl;
    private final int maxSize;
    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong misses = new AtomicLong();
    private final AtomicLong evictions = new AtomicLong();
    private final boolean trackStats;

    @SuppressWarnings("unchecked")
    public SegmentedCache(final CacheConfig config) {
        this.ttl = config.ttl();
        this.maxSize = config.maxSize();
        this.trackStats = config.enableStats();
        this.segments = new Segment[SEGMENT_COUNT];
        for (int i = 0; i < SEGMENT_COUNT; i++) {
            segments[i] = new Segment<>();
        }
    }

    private Segment<K, V> segmentFor(final K key) {
        return segments[(key.hashCode() & 0x7FFFFFFF) % SEGMENT_COUNT];
    }

    @Override
    public Optional<V> get(final K key) {
        final Segment<K, V> segment = segmentFor(key);
        final CacheEntry<V> entry = segment.map.get(key);
        if (entry == null || entry.isExpired(ttl)) {
            if (entry != null) {
                segment.map.remove(key);
                if (trackStats) {
                    evictions.incrementAndGet();
                }
            }
            if (trackStats) {
                misses.incrementAndGet();
            }
            return Optional.empty();
        }
        if (trackStats) {
            hits.incrementAndGet();
        }
        return Optional.of(entry.value);
    }

    @Override
    public V getOrLoad(final K key, final Function<K, V> loader) {
        return get(key).orElseGet(() -> {
            final V value = loader.apply(key);
            put(key, value);
            return value;
        });
    }

    @Override
    public void put(final K key, final V value) {
        final Segment<K, V> segment = segmentFor(key);
        segment.map.put(key, new CacheEntry<>(value));
        evictIfNeeded();
    }

    @Override
    public void invalidate(final K key) {
        segmentFor(key).map.remove(key);
    }

    @Override
    public void invalidateAll() {
        for (final Segment<K, V> segment : segments) {
            segment.map.clear();
        }
    }

    @Override
    public long size() {
        long total = 0;
        for (final Segment<K, V> segment : segments) {
            total += segment.map.size();
        }
        return total;
    }

    @Override
    public CacheStats stats() {
        return new CacheStats(hits.get(), misses.get(), evictions.get());
    }

    public void cleanup() {
        for (final Segment<K, V> segment : segments) {
            segment.map.entrySet().removeIf(e -> e.getValue().isExpired(ttl));
        }
    }

    private void evictIfNeeded() {
        if (maxSize > 0 && size() > maxSize) {
            for (final Segment<K, V> segment : segments) {
                final var iterator = segment.map.entrySet().iterator();
                while (iterator.hasNext() && size() > maxSize) {
                    iterator.next();
                    iterator.remove();
                    if (trackStats) {
                        evictions.incrementAndGet();
                    }
                }
            }
        }
    }

    private static final class Segment<K, V> {
        final ConcurrentHashMap<K, CacheEntry<V>> map = new ConcurrentHashMap<>();
    }

    private static final class CacheEntry<V> {
        final V value;
        final long createdAt;

        CacheEntry(final V value) {
            this.value = value;
            this.createdAt = System.currentTimeMillis();
        }

        boolean isExpired(final Duration ttl) {
            if (ttl == null || ttl.isZero()) {
                return false;
            }
            return System.currentTimeMillis() - createdAt > ttl.toMillis();
        }
    }
}
