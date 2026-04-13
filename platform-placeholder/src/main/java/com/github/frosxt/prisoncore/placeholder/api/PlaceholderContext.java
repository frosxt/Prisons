package com.github.frosxt.prisoncore.placeholder.api;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * Contextual data passed to placeholder resolvers. The player id may be {@code null}
 * when the template is being processed outside a player scope (broadcasts, logs).
 */
public final class PlaceholderContext {
    private final UUID playerId;
    private final Map<String, String> extra;

    public PlaceholderContext(final UUID playerId, final Map<String, String> extra) {
        this.playerId = playerId;
        this.extra = extra != null ? Collections.unmodifiableMap(extra) : Collections.emptyMap();
    }

    public PlaceholderContext(final UUID playerId) {
        this(playerId, Collections.emptyMap());
    }

    public UUID playerId() {
        return playerId;
    }

    public Map<String, String> extra() {
        return extra;
    }
}
