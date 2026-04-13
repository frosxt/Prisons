package com.github.frosxt.prisoncore.scheduler.bukkit;

import com.github.frosxt.prisoncore.scheduler.api.TaskHandle;
import com.github.frosxt.prisoncore.scheduler.api.TaskOrchestrator;
import com.github.frosxt.prisoncore.scheduler.api.TaskSpec;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class BukkitTaskOrchestrator implements TaskOrchestrator {
    private final Plugin plugin;
    private final ScheduledExecutorService ioExecutor;
    private final ScheduledExecutorService cpuExecutor;

    public BukkitTaskOrchestrator(final Plugin plugin) {
        this.plugin = plugin;
        this.ioExecutor = new ScheduledThreadPoolExecutor(
                Math.max(2, Runtime.getRuntime().availableProcessors()),
                r -> {
                    final Thread t = new Thread(r, "PrisonCore-IO");
                    t.setDaemon(true);
                    return t;
                });
        this.cpuExecutor = new ScheduledThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors(),
                r -> {
                    final Thread t = new Thread(r, "PrisonCore-CPU");
                    t.setDaemon(true);
                    return t;
                });
    }

    @Override
    public TaskHandle mainThread(final TaskSpec spec) {
        final long delayTicks = toTicks(spec.delay());
        final BukkitTask task;
        if (spec.isRepeating()) {
            final long periodTicks = toTicks(spec.period());
            task = Bukkit.getScheduler().runTaskTimer(plugin, spec.task(), delayTicks, periodTicks);
        } else {
            task = Bukkit.getScheduler().runTaskLater(plugin, spec.task(), delayTicks);
        }
        return new BukkitTaskHandle(task);
    }

    @Override
    public TaskHandle io(final TaskSpec spec) {
        return scheduleOn(ioExecutor, spec);
    }

    @Override
    public TaskHandle cpu(final TaskSpec spec) {
        return scheduleOn(cpuExecutor, spec);
    }

    @Override
    public CompletableFuture<Void> switchToMainThread() {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(plugin, () -> future.complete(null));
        return future;
    }

    public void shutdown() {
        ioExecutor.shutdown();
        cpuExecutor.shutdown();
    }

    private TaskHandle scheduleOn(final ScheduledExecutorService executor, final TaskSpec spec) {
        final long delayMillis = spec.delay() != null ? spec.delay().toMillis() : 0;

        final ScheduledFuture<?> future;
        if (spec.isRepeating()) {
            final long periodMillis = spec.period().toMillis();
            future = executor.scheduleAtFixedRate(spec.task(), delayMillis, periodMillis, TimeUnit.MILLISECONDS);
        } else {
            future = executor.schedule(spec.task(), delayMillis, TimeUnit.MILLISECONDS);
        }
        return new ScheduledFutureTaskHandle(future);
    }

    private long toTicks(final java.time.Duration duration) {
        if (duration == null) {
            return 0;
        }
        return duration.toMillis() / 50;
    }

    private record BukkitTaskHandle(BukkitTask task) implements TaskHandle {
        @Override
        public int id() {
            return task.getTaskId();
        }

        @Override
        public void cancel() {
            task.cancel();
        }

        @Override
        public boolean isCancelled() {
            return task.isCancelled();
        }
    }

    private record ScheduledFutureTaskHandle(ScheduledFuture<?> future) implements TaskHandle {
        @Override
        public int id() {
            return -1;
        }

        @Override
        public void cancel() {
            future.cancel(true);
        }

        @Override
        public boolean isCancelled() {
            return future.isCancelled();
        }
    }
}
