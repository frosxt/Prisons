package com.github.frosxt.prisoncore.api.event.events;

import com.github.frosxt.prisoncore.api.event.DomainEvent;

import java.util.UUID;

public final class PlayerJoinedEvent extends DomainEvent {
    private final UUID playerId;
    private final String username;

    public PlayerJoinedEvent(final UUID playerId, final String username) {
        super("bukkit");
        this.playerId = playerId;
        this.username = username;
    }

    public UUID playerId() {
        return playerId;
    }

    public String username() {
        return username;
    }
}
