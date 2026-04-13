package com.github.frosxt.prisoncore.kernel.shutdown;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ShutdownBarrier {
    private final AtomicBoolean engaged = new AtomicBoolean(false);
    private final List<Runnable> flushTasks = new CopyOnWriteArrayList<>();
    private final Logger logger;

    public ShutdownBarrier(final Logger logger) {
        this.logger = logger;
    }

    public boolean isEngaged() {
        return engaged.get();
    }

    public void registerFlushTask(final Runnable task) {
        flushTasks.add(task);
    }

    public void engage(final long timeoutMs) {
        if (!engaged.compareAndSet(false, true)) {
            return;
        }
        logger.info("[PrisonCore] Shutdown barrier engaged. Flushing pending operations...");

        for (final Runnable task : flushTasks) {
            try {
                task.run();
            } catch (final Exception e) {
                logger.log(Level.WARNING, "[PrisonCore] Flush task failed during shutdown", e);
            }
        }

        logger.info("[PrisonCore] All flush tasks completed.");
    }
}
