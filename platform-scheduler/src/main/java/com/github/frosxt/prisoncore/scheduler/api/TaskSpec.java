package com.github.frosxt.prisoncore.scheduler.api;

import java.time.Duration;

/**
 * Immutable description of a scheduled task. Construct via {@link #builder(Runnable)};
 * a non-null {@code period} marks the spec as repeating.
 */
public final class TaskSpec {
    private final Runnable task;
    private final Duration delay;
    private final Duration period;

    private TaskSpec(final Builder builder) {
        this.task = builder.task;
        this.delay = builder.delay;
        this.period = builder.period;
    }

    public static Builder builder(final Runnable task) {
        return new Builder(task);
    }

    public Runnable task() {
        return task;
    }

    public Duration delay() {
        return delay;
    }

    public Duration period() {
        return period;
    }

    public boolean isRepeating() {
        return period != null && !period.isZero();
    }

    public static final class Builder {
        private final Runnable task;
        private Duration delay = Duration.ZERO;
        private Duration period;

        private Builder(final Runnable task) {
            this.task = task;
        }

        public Builder delay(final Duration delay) {
            this.delay = delay;
            return this;
        }

        public Builder period(final Duration period) {
            this.period = period;
            return this;
        }

        public TaskSpec build() {
            return new TaskSpec(this);
        }
    }
}
