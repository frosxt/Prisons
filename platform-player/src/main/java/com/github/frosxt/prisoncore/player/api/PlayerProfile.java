package com.github.frosxt.prisoncore.player.api;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerProfile {
    private final UUID id;
    private String username;
    private final Instant firstSeen;
    private Instant lastSeen;
    private final Map<String, Object> metadata;

    public PlayerProfile(final UUID id, final String username, final Instant firstSeen, final Instant lastSeen) {
        this.id = id;
        this.username = username;
        this.firstSeen = firstSeen;
        this.lastSeen = lastSeen;
        this.metadata = new ConcurrentHashMap<>();
    }

    public static PlayerProfile createNew(final UUID id, final String username) {
        final Instant now = Instant.now();
        return new PlayerProfile(id, username, now, now);
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

    public void setUsername(final String username) {
        this.username = username;
    }

    public void setLastSeen(final Instant lastSeen) {
        this.lastSeen = lastSeen;
    }

    @SuppressWarnings("unchecked")
    public <T> T getMetadata(final String key, final Class<T> type) {
        return (T) metadata.get(key);
    }

    public void setMetadata(final String key, final Object value) {
        metadata.put(key, value);
    }
}
