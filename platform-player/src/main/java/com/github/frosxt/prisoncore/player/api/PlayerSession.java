package com.github.frosxt.prisoncore.player.api;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerSession {
    private final UUID playerId;
    private final PlayerProfile profile;
    private final Instant loginTime;
    private final Map<String, Object> sessionData;

    public PlayerSession(final UUID playerId, final PlayerProfile profile) {
        this.playerId = playerId;
        this.profile = profile;
        this.loginTime = Instant.now();
        this.sessionData = new ConcurrentHashMap<>();
    }

    public UUID playerId() {
        return playerId;
    }

    public PlayerProfile profile() {
        return profile;
    }

    public Instant loginTime() {
        return loginTime;
    }

    @SuppressWarnings("unchecked")
    public <T> T getData(final String key, final Class<T> type) {
        return (T) sessionData.get(key);
    }

    public void setData(final String key, final Object value) {
        sessionData.put(key, value);
    }

    public void removeData(final String key) {
        sessionData.remove(key);
    }
}
