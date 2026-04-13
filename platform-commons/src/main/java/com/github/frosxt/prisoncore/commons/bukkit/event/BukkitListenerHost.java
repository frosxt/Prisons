package com.github.frosxt.prisoncore.commons.bukkit.event;

import org.bukkit.event.Listener;

/**
 * Registers raw Bukkit {@link Listener} instances through the platform's host plugin
 * so modules never need to look up the JavaPlugin themselves or call
 * {@code Bukkit.getPluginManager().registerEvents(...)} directly.
 *
 * Modules should only use this when a raw Bukkit event is actually required.
 * For domain events (player join/quit, platform state changes), subscribe via
 * {@code DomainEventBus} instead.
 */
public interface BukkitListenerHost {

    void register(Listener listener);

    void unregister(Listener listener);
}
