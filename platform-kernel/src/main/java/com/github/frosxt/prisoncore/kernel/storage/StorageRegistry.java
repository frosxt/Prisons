package com.github.frosxt.prisoncore.kernel.storage;

import com.github.frosxt.prisoncore.spi.storage.StorageBackend;
import com.github.frosxt.prisoncore.spi.storage.StorageBackendFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central registry of {@link StorageBackend} instances. Modules resolve this from
 * the service container and call {@link #getOrCreate(String, String, Map)} to acquire
 * a backend handle; the registry owns connection lifecycle and shuts everything down
 * on kernel disable.
 */
public final class StorageRegistry {
    private final Map<String, StorageBackendFactory> factories = new ConcurrentHashMap<>();
    private final Map<String, StorageBackend> activeBackends = new ConcurrentHashMap<>();
    private final Logger logger;

    public StorageRegistry(final Logger logger) {
        this.logger = logger;
    }

    public void registerFactory(final StorageBackendFactory factory) {
        factories.put(factory.type(), factory);
        logger.info("[PrisonCore] Registered storage backend factory: " + factory.type());
    }

    /**
     * Get or create a backend instance. The scope parameter allows multiple independent
     * backends of the same type (e.g., two SQL connections for different modules).
     * Pass null or empty for the default/shared instance.
     */
    public StorageBackend getOrCreate(final String type, final String scope, final Map<String, Object> config) {
        final String key = scope != null && !scope.isEmpty() ? type + ":" + scope : type;
        final StorageBackend existing = activeBackends.get(key);
        if (existing != null && existing.isHealthy()) {
            return existing;
        }

        final StorageBackendFactory factory = factories.get(type);
        if (factory == null) {
            throw new IllegalStateException("No storage backend factory registered for type: " + type + ". Available: " + factories.keySet());
        }

        final StorageBackend backend = factory.create(config);
        try {
            backend.connect();
            activeBackends.put(key, backend);
            logger.info("[PrisonCore] Storage backend connected: " + key);
            return backend;
        } catch (final Exception e) {
            logger.log(Level.SEVERE, "[PrisonCore] Failed to connect storage backend: " + key, e);
            throw new RuntimeException("Storage backend connection failed: " + key, e);
        }
    }

    /** Convenience overload for the default/shared instance of a backend type. */
    public StorageBackend getOrCreate(final String type, final Map<String, Object> config) {
        return getOrCreate(type, null, config);
    }

    public Optional<StorageBackend> getActive(final String type) {
        return Optional.ofNullable(activeBackends.get(type));
    }

    public Optional<StorageBackend> getActive(final String type, final String scope) {
        final String key = scope != null && !scope.isEmpty() ? type + ":" + scope : type;
        return Optional.ofNullable(activeBackends.get(key));
    }

    public boolean hasFactory(final String type) {
        return factories.containsKey(type);
    }

    public void shutdownAll() {
        for (final Map.Entry<String, StorageBackend> entry : activeBackends.entrySet()) {
            try {
                entry.getValue().disconnect();
                logger.info("[PrisonCore] Disconnected storage backend: " + entry.getKey());
            } catch (final Exception e) {
                logger.log(Level.WARNING, "[PrisonCore] Error disconnecting storage: " + entry.getKey(), e);
            }
        }
        activeBackends.clear();
    }

    public Set<String> availableTypes() {
        return Collections.unmodifiableSet(factories.keySet());
    }
}
