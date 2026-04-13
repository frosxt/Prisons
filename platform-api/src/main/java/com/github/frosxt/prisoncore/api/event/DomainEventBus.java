package com.github.frosxt.prisoncore.api.event;

/**
 * Cross-module event bus for platform-neutral domain events. Prefer subscribing
 * to domain events here over registering raw Bukkit listeners — Bukkit join/quit
 * events are bridged into {@code PlayerJoinedEvent} / {@code PlayerLeftEvent}
 * automatically at the runtime layer.
 */
public interface DomainEventBus {
    void publish(DomainEvent event);

    /** Subscribe a handler for a specific event type. */
    <T extends DomainEvent> void subscribe(Class<T> eventType, DomainEventHandler<T> handler);

    <T extends DomainEvent> void unsubscribe(Class<T> eventType, DomainEventHandler<T> handler);
}
