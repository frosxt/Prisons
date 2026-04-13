package com.github.frosxt.prisoncore.kernel.container.session;

import com.github.frosxt.prisoncore.api.service.ServiceContainer;
import com.github.frosxt.prisoncore.api.service.ServiceException;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A lightweight per-player scope that is intentionally <em>not</em> a
 * {@link ServiceContainer.Scope}. Services not registered in the session are
 * resolved by delegating to the kernel container — this is by design, not an oversight.
 */
public final class SessionScope implements AutoCloseable {
    private final UUID playerId;
    private final ServiceContainer parent;
    private final Map<Class<?>, Object> sessionServices = new ConcurrentHashMap<>();

    public SessionScope(final UUID playerId, final ServiceContainer parent) {
        this.playerId = playerId;
        this.parent = parent;
    }

    public UUID playerId() {
        return playerId;
    }

    public <T> void register(final Class<T> type, final T instance) {
        final Object existing = sessionServices.putIfAbsent(type, instance);
        if (existing != null) {
            throw new ServiceException("Duplicate session service for player " + playerId + ": " + type.getName());
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T resolve(final Class<T> type) {
        final Object local = sessionServices.get(type);
        if (local != null) {
            return (T) local;
        }
        return parent.resolve(type);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> resolveOptional(final Class<T> type) {
        final Object local = sessionServices.get(type);
        if (local != null) {
            return Optional.of((T) local);
        }
        return parent.resolveOptional(type);
    }

    @Override
    public void close() {
        for (final Object service : sessionServices.values()) {
            if (service instanceof final AutoCloseable closeable) {
                try {
                    closeable.close();
                } catch (final Exception ignored) {
                    // ignored
                }
            }
        }
        sessionServices.clear();
    }
}
