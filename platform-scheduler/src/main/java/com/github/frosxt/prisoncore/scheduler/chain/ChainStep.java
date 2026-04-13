package com.github.frosxt.prisoncore.scheduler.chain;

import java.time.Duration;

final class ChainStep {
    private final Runnable action;
    private final Duration delay;
    private final ThreadContext context;
    private final int repetitions;

    ChainStep(final Runnable action, final Duration delay, final ThreadContext context, final int repetitions) {
        this.action = action;
        this.delay = delay;
        this.context = context;
        this.repetitions = repetitions;
    }

    Runnable action() {
        return action;
    }

    Duration delay() {
        return delay;
    }

    ThreadContext context() {
        return context;
    }

    int repetitions() {
        return repetitions;
    }
}
