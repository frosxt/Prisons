package com.github.frosxt.prisoncore.commons.api.cache;

public final class CacheStats {
    private final long hits;
    private final long misses;
    private final long evictions;

    public CacheStats(final long hits, final long misses, final long evictions) {
        this.hits = hits;
        this.misses = misses;
        this.evictions = evictions;
    }

    public long hits() {
        return hits;
    }

    public long misses() {
        return misses;
    }

    public long evictions() {
        return evictions;
    }
    public double hitRate() {
        final long total = hits + misses;
        return total == 0 ? 0.0 : (double) hits / total;
    }
}
