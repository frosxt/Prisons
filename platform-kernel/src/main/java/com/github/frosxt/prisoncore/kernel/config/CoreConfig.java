package com.github.frosxt.prisoncore.kernel.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable, platform-wide configuration parsed from {@code core.yml} by the runtime.
 * Modules resolve this from the {@link com.github.frosxt.prisoncore.api.service.ServiceContainer}
 * to discover the selected storage backend and backend-specific settings.
 */
public final class CoreConfig {
    private final boolean debugMode;
    private final String storageBackend;
    private final Map<String, String> storageProperties;
    private final String defaultCurrencyId;

    public CoreConfig(final boolean debugMode, final String storageBackend,
                      final Map<String, String> storageProperties, final String defaultCurrencyId) {
        this.debugMode = debugMode;
        this.storageBackend = storageBackend;
        this.storageProperties = Collections.unmodifiableMap(new LinkedHashMap<>(storageProperties));
        this.defaultCurrencyId = defaultCurrencyId;
    }

    public static CoreConfig defaults() {
        return new CoreConfig(false, "json", Collections.emptyMap(), "money");
    }

    public boolean debugMode() {
        return debugMode;
    }

    /** @return the storage backend identifier (json, sqlite, sql, mongo). */
    public String storageBackend() {
        return storageBackend;
    }

    /** @return backend-specific key/value settings; keys not related to the active backend are ignored. */
    public Map<String, String> storageProperties() {
        return storageProperties;
    }

    public String defaultCurrencyId() {
        return defaultCurrencyId;
    }
}
