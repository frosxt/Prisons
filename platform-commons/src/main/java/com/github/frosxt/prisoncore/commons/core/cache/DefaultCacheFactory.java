package com.github.frosxt.prisoncore.commons.core.cache;

import com.github.frosxt.prisoncore.commons.api.cache.Cache;
import com.github.frosxt.prisoncore.commons.api.cache.CacheConfig;
import com.github.frosxt.prisoncore.commons.api.cache.CacheFactory;

public final class DefaultCacheFactory implements CacheFactory {
    @Override
    public <K, V> Cache<K, V> create(final String name, final CacheConfig config) {
        return new SegmentedCache<>(config);
    }
}
