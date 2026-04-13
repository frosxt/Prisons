package com.github.frosxt.prisoncore.config.yaml;

import com.github.frosxt.prisoncore.config.api.ConfigService;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class YamlConfigService implements ConfigService {
    private final Path configDir;
    private final Logger logger;
    private final Map<String, YamlConfiguration> loaded = new ConcurrentHashMap<>();

    public YamlConfigService(final Path configDir, final Logger logger) {
        this.configDir = configDir;
        this.logger = logger;
    }

    @Override
    public Map<String, Object> load(final String name) {
        final File file = configDir.resolve(name + ".yml").toFile();
        if (!file.exists()) {
            return Collections.emptyMap();
        }
        final YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        loaded.put(name, config);
        final Map<String, Object> result = new LinkedHashMap<>();
        for (final String key : config.getKeys(true)) {
            if (!config.isConfigurationSection(key)) {
                result.put(key, config.get(key));
            }
        }
        return result;
    }

    @Override
    public void save(final String name, final Map<String, Object> data) {
        final File file = configDir.resolve(name + ".yml").toFile();
        final YamlConfiguration config = loaded.getOrDefault(name, new YamlConfiguration());
        for (final Map.Entry<String, Object> entry : data.entrySet()) {
            config.set(entry.getKey(), entry.getValue());
        }
        try {
            file.getParentFile().mkdirs();
            config.save(file);
            loaded.put(name, config);
        } catch (final IOException e) {
            logger.log(Level.SEVERE, "Failed to save config: " + name, e);
        }
    }

    @Override
    public void reload(final String name) {
        loaded.remove(name);
        load(name);
    }

    @Override
    public Optional<Object> get(final String name, final String key) {
        YamlConfiguration config = loaded.get(name);
        if (config == null) {
            load(name);
            config = loaded.get(name);
        }
        return config != null ? Optional.ofNullable(config.get(key)) : Optional.empty();
    }

    @Override
    public void set(final String name, final String key, final Object value) {
        final YamlConfiguration config = loaded.computeIfAbsent(name, k -> new YamlConfiguration());
        config.set(key, value);
    }
}
