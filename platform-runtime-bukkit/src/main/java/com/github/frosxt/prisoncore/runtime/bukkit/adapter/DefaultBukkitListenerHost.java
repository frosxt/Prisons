package com.github.frosxt.prisoncore.runtime.bukkit.adapter;

import com.github.frosxt.prisoncore.commons.bukkit.event.BukkitListenerHost;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class DefaultBukkitListenerHost implements BukkitListenerHost {

    private final JavaPlugin plugin;

    public DefaultBukkitListenerHost(final JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void register(final Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, plugin);
    }

    @Override
    public void unregister(final Listener listener) {
        HandlerList.unregisterAll(listener);
    }
}
