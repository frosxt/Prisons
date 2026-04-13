package com.github.frosxt.prisoncore.kernel.observability;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public final class ObservabilityService {
    private final Map<String, LongAdder> counters = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> gauges = new ConcurrentHashMap<>();
    private final Map<String, TimingStats> timings = new ConcurrentHashMap<>();

    public void incrementCounter(final String name) {
        counters.computeIfAbsent(name, k -> new LongAdder()).increment();
    }

    public void incrementCounter(final String name, final long amount) {
        counters.computeIfAbsent(name, k -> new LongAdder()).add(amount);
    }

    public void setGauge(final String name, final long value) {
        gauges.computeIfAbsent(name, k -> new AtomicLong()).set(value);
    }

    public void recordTiming(final String name, final long durationMs) {
        timings.computeIfAbsent(name, k -> new TimingStats()).record(durationMs);
    }

    public long getCounter(final String name) {
        final LongAdder adder = counters.get(name);
        return adder != null ? adder.sum() : 0;
    }

    public long getGauge(final String name) {
        final AtomicLong gauge = gauges.get(name);
        return gauge != null ? gauge.get() : 0;
    }

    public TimingStats getTiming(final String name) {
        return timings.get(name);
    }

    public Map<String, Long> allCounters() {
        final Map<String, Long> result = new ConcurrentHashMap<>();
        counters.forEach((k, v) -> result.put(k, v.sum()));
        return result;
    }

    public Map<String, Long> allGauges() {
        final Map<String, Long> result = new ConcurrentHashMap<>();
        gauges.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }

    public static final class TimingStats {
        private final LongAdder count = new LongAdder();
        private final LongAdder totalMs = new LongAdder();
        private volatile long minMs = Long.MAX_VALUE;
        private volatile long maxMs = 0;

        void record(final long durationMs) {
            count.increment();
            totalMs.add(durationMs);
            if (durationMs < minMs) {
                minMs = durationMs;
            }
            if (durationMs > maxMs) {
                maxMs = durationMs;
            }
        }

        public long count() {
            return count.sum();
        }

        public long totalMs() {
            return totalMs.sum();
        }

        public long minMs() {
            return minMs == Long.MAX_VALUE ? 0 : minMs;
        }

        public long maxMs() {
            return maxMs;
        }

        public double avgMs() {
            return count.sum() == 0 ? 0 : (double) totalMs.sum() / count.sum();
        }
    }
}
