package com.github.frosxt.prisoncore.commons.api.cache;

import java.util.Optional;
import java.util.function.Function;

public interface Cache<K, V> {
    Optional<V> get(K key);

    V getOrLoad(K key, Function<K, V> loader);
    void put(K key, V value);
    void invalidate(K key);
    void invalidateAll();
    long size();
    CacheStats stats();
}
