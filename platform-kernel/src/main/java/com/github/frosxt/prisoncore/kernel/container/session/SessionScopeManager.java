package com.github.frosxt.prisoncore.kernel.container.session;

import com.github.frosxt.prisoncore.api.service.ServiceContainer;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionScopeManager {
    private final ServiceContainer parent;
    private final Map<UUID, SessionScope> activeSessions = new ConcurrentHashMap<>();

    public SessionScopeManager(final ServiceContainer parent) {
        this.parent = parent;
    }

    public SessionScope create(final UUID playerId) {
        final SessionScope scope = new SessionScope(playerId, parent);
        activeSessions.put(playerId, scope);
        return scope;
    }

    public Optional<SessionScope> get(final UUID playerId) {
        return Optional.ofNullable(activeSessions.get(playerId));
    }

    public void destroy(final UUID playerId) {
        final SessionScope scope = activeSessions.remove(playerId);
        if (scope != null) {
            scope.close();
        }
    }

    public void destroyAll() {
        for (final SessionScope scope : activeSessions.values()) {
            scope.close();
        }
        activeSessions.clear();
    }

    public int activeCount() {
        return activeSessions.size();
    }
}
