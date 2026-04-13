package com.github.frosxt.prisoncore.api.event.events;

import com.github.frosxt.prisoncore.api.event.DomainEvent;

import java.util.UUID;

public final class PlayerLeftEvent extends DomainEvent {
    private final UUID playerId;

    public PlayerLeftEvent(final UUID playerId) {
        super("bukkit");
        this.playerId = playerId;
    }

    public UUID playerId() {
        return playerId;
    }
}
