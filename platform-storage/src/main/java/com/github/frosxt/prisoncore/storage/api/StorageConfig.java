package com.github.frosxt.prisoncore.storage.api;

import java.util.Collections;
import java.util.Map;

public final class StorageConfig {
    private final StorageProfile profile;
    private final String backendType;
    private final Map<String, String> properties;

    public StorageConfig(final StorageProfile profile, final String backendType, final Map<String, String> properties) {
        this.profile = profile;
        this.backendType = backendType;
        this.properties = Collections.unmodifiableMap(properties);
    }

    public StorageProfile profile() {
        return profile;
    }

    public String backendType() {
        return backendType;
    }

    public Map<String, String> properties() {
        return properties;
    }

    public String property(final String key) {
        return properties.get(key);
    }

    public String property(final String key, final String defaultValue) {
        return properties.getOrDefault(key, defaultValue);
    }
}
