package com.github.frosxt.prisoncore.commons.core.cooldown;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CooldownTracker {
    private final Map<String, Map<UUID, Instant>> cooldowns = new ConcurrentHashMap<>();

    public boolean isOnCooldown(final UUID playerId, final String action) {
        final Map<UUID, Instant> actionCooldowns = cooldowns.get(action);
        if (actionCooldowns == null) {
            return false;
        }
        final Instant expiry = actionCooldowns.get(playerId);
        if (expiry == null) {
            return false;
        }
        if (Instant.now().isAfter(expiry)) {
            actionCooldowns.remove(playerId);
            return false;
        }
        return true;
    }

    public Optional<Duration> remaining(final UUID playerId, final String action) {
        final Map<UUID, Instant> actionCooldowns = cooldowns.get(action);
        if (actionCooldowns == null) {
            return Optional.empty();
        }
        final Instant expiry = actionCooldowns.get(playerId);
        if (expiry == null || Instant.now().isAfter(expiry)) {
            return Optional.empty();
        }
        return Optional.of(Duration.between(Instant.now(), expiry));
    }

    public void setCooldown(final UUID playerId, final String action, final Duration duration) {
        cooldowns.computeIfAbsent(action, k -> new ConcurrentHashMap<>()).put(playerId, Instant.now().plus(duration));
    }

    public void clearCooldown(final UUID playerId, final String action) {
        final Map<UUID, Instant> actionCooldowns = cooldowns.get(action);
        if (actionCooldowns != null) {
            actionCooldowns.remove(playerId);
        }
    }

    public void clearAll(final UUID playerId) {
        cooldowns.values().forEach(map -> map.remove(playerId));
    }
}
