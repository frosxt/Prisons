package com.github.frosxt.prisoncore.storage.json;

import com.github.frosxt.prisoncore.spi.storage.StorageBackend;
import com.github.frosxt.prisoncore.spi.storage.StorageBackendFactory;

import java.nio.file.Paths;
import java.util.Map;

public final class JsonStorageBackendFactory implements StorageBackendFactory {

    @Override
    public String type() {
        return "json";
    }

    @Override
    public StorageBackend create(final Map<String, Object> config) {
        final String directory = config.getOrDefault("directory", "data").toString();
        return new JsonStorageBackend(Paths.get(directory));
    }
}
