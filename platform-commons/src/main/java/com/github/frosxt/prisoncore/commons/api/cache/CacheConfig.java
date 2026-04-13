package com.github.frosxt.prisoncore.commons.api.cache;

import java.time.Duration;

public final class CacheConfig {
    private final Duration ttl;
    private final int maxSize;
    private final boolean enableStats;

    private CacheConfig(final Builder builder) {
        this.ttl = builder.ttl;
        this.maxSize = builder.maxSize;
        this.enableStats = builder.enableStats;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Duration ttl() {
        return ttl;
    }

    public int maxSize() {
        return maxSize;
    }

    public boolean enableStats() {
        return enableStats;
    }

    public static final class Builder {
        private Duration ttl = Duration.ofMinutes(10);
        private int maxSize = 1000;
        private boolean enableStats = true;

        public Builder ttl(final Duration ttl) {
            this.ttl = ttl;
            return this;
        }

        public Builder maxSize(final int maxSize) {
            this.maxSize = maxSize;
            return this;
        }

        public Builder enableStats(final boolean enableStats) {
            this.enableStats = enableStats;
            return this;
        }

        public CacheConfig build() {
            return new CacheConfig(this);
        }
    }
}
