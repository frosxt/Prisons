package com.github.frosxt.prisoncore.config.api;

import java.util.Map;
import java.util.Optional;

/**
 * Generic YAML-backed config store used by the platform for files outside the
 * {@code core.yml} path. Named config files are referenced by the file stem
 * (no extension).
 */
public interface ConfigService {
    /** Load the entire config as a nested map. */
    Map<String, Object> load(String name);
    void save(String name, Map<String, Object> data);
    void reload(String name);
    /** Retrieve a single value by dotted path, e.g. {@code storage.backend}. */
    Optional<Object> get(String name, String key);
    void set(String name, String key, Object value);
}
