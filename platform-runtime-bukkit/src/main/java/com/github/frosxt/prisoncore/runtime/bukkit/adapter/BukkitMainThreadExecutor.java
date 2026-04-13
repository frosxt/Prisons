package com.github.frosxt.prisoncore.runtime.bukkit.adapter;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

public final class BukkitMainThreadExecutor {
    private final Plugin plugin;

    public BukkitMainThreadExecutor(final Plugin plugin) {
        this.plugin = plugin;
    }

    public void execute(final Runnable task) {
        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    public <T> CompletableFuture<T> submit(final Callable<T> callable) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        execute(() -> {
            try {
                future.complete(callable.call());
            } catch (final Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }
}
