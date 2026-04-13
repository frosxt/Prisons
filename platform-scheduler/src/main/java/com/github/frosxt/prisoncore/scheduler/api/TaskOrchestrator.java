package com.github.frosxt.prisoncore.scheduler.api;

import java.util.concurrent.CompletableFuture;

/**
 * Single entry point for scheduling work. Modules must not call
 * {@code Bukkit.getScheduler()} directly — route all task submission through here.
 *
 * <p>Pick the execution context that matches the work: {@link #mainThread} for any
 * operation that touches Bukkit world/entity state, {@link #io} for disk/database
 * work, {@link #cpu} for computation-bound work that must not block IO threads.
 */
public interface TaskOrchestrator {

    /** Schedule on the Bukkit main thread. Required for entity/world/inventory access. */
    TaskHandle mainThread(TaskSpec spec);

    /** Schedule on the shared IO pool. Use for disk, database, and network calls. */
    TaskHandle io(TaskSpec spec);

    /** Schedule on the shared CPU pool (sized to available cores). */
    TaskHandle cpu(TaskSpec spec);

    /**
     * Hop the caller's continuation onto the Bukkit main thread.
     * @return a future that completes on the main thread.
     */
    CompletableFuture<Void> switchToMainThread();
}
