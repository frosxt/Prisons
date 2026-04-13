package com.github.frosxt.prisoncore.storage.sql;

import com.github.frosxt.prisoncore.spi.storage.StorageBackend;
import com.github.frosxt.prisoncore.spi.storage.StorageBackendFactory;

import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class SqlStorageBackendFactory implements StorageBackendFactory {
    private final Logger logger;

    public SqlStorageBackendFactory(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public String type() {
        return "sql";
    }

    @Override
    public StorageBackend create(final Map<String, Object> config) {
        final Map<String, String> stringConfig = config.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
        return new SqlBackend(stringConfig, logger);
    }
}
