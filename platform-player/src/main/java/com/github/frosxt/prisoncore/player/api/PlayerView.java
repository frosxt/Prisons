package com.github.frosxt.prisoncore.player.api;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Immutable snapshot of a player profile for read-heavy UI/placeholder access.
 * Takes a deep copy of all mutable state at construction time.
 */
public final class PlayerView {
    private final UUID id;
    private final String username;
    private final Instant firstSeen;
    private final Instant lastSeen;
    private final Map<String, Object> metadata;

    public PlayerView(final PlayerProfile profile) {
        this.id = profile.id();
        this.username = profile.username();
        this.firstSeen = profile.firstSeen();
        this.lastSeen = profile.lastSeen();
        this.metadata = Collections.unmodifiableMap(new LinkedHashMap<>(profile.metadata()));
    }

    public UUID id() {
        return id;
    }

    public String username() {
        return username;
    }

    public Instant firstSeen() {
        return firstSeen;
    }

    public Instant lastSeen() {
        return lastSeen;
    }

    public Map<String, Object> metadata() {
        return metadata;
    }
}
