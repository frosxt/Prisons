package com.github.frosxt.prisoncore.runtime.bukkit.bridge;

import com.github.frosxt.prisoncore.api.event.DomainEventBus;
import com.github.frosxt.prisoncore.api.event.events.PlayerJoinedEvent;
import com.github.frosxt.prisoncore.api.event.events.PlayerLeftEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class BukkitEventBridge implements Listener {
    private final DomainEventBus eventBus;

    public BukkitEventBridge(final DomainEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        eventBus.publish(new PlayerJoinedEvent(event.getPlayer().getUniqueId(), event.getPlayer().getName()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        eventBus.publish(new PlayerLeftEvent(event.getPlayer().getUniqueId()));
    }
}
