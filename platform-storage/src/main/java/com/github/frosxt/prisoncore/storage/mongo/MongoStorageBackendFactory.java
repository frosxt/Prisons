package com.github.frosxt.prisoncore.storage.mongo;

import com.github.frosxt.prisoncore.spi.storage.StorageBackend;
import com.github.frosxt.prisoncore.spi.storage.StorageBackendFactory;

import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class MongoStorageBackendFactory implements StorageBackendFactory {
    private final Logger logger;

    public MongoStorageBackendFactory(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public String type() {
        return "mongo";
    }

    @Override
    public StorageBackend create(final Map<String, Object> config) {
        final Map<String, String> stringConfig = config.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
        return new MongoBackend(stringConfig, logger);
    }
}
