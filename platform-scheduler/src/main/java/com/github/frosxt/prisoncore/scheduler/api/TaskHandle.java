package com.github.frosxt.prisoncore.scheduler.api;

/**
 * Reference to a scheduled task, returned by {@link TaskOrchestrator}.
 * Hold onto the handle and call {@link #cancel()} from your module's {@code onDisable}
 * to stop repeating tasks cleanly.
 */
public interface TaskHandle {
    int id();
    void cancel();
    boolean isCancelled();
}
