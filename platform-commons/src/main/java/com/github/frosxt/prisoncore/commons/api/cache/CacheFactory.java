package com.github.frosxt.prisoncore.commons.api.cache;

public interface CacheFactory {
    <K, V> Cache<K, V> create(String name, CacheConfig config);
}
