package com.github.frosxt.prisoncore.storage.sqlite;

import com.github.frosxt.prisoncore.spi.storage.StorageBackend;
import com.github.frosxt.prisoncore.spi.storage.StorageBackendFactory;

import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Logger;

public final class SqliteStorageBackendFactory implements StorageBackendFactory {
    private final Logger logger;

    public SqliteStorageBackendFactory(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public String type() {
        return "sqlite";
    }

    @Override
    public StorageBackend create(final Map<String, Object> config) {
        final String file = config.getOrDefault("file", "data/prisoncore.db").toString();
        return new SqliteBackend(Paths.get(file), logger);
    }
}
