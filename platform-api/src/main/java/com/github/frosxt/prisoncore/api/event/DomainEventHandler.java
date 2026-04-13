package com.github.frosxt.prisoncore.api.event;

public interface DomainEventHandler<T extends DomainEvent> {
    void handle(T event);

    default int priority() {
        return 0;
    }

    default boolean async() {
        return false;
    }
}
