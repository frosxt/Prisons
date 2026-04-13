package com.github.frosxt.prisoncore.storage.json;

import com.github.frosxt.prisoncore.spi.storage.StorageBackend;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Filesystem-backed storage backend. The "handle" is the root data directory;
 * modules write their own files underneath it with whatever schema they need.
 */
public final class JsonStorageBackend implements StorageBackend {

    private final Path directory;

    public JsonStorageBackend(final Path directory) {
        this.directory = directory;
    }

    @Override
    public String name() {
        return "json";
    }

    @Override
    public void connect() throws IOException {
        Files.createDirectories(directory);
    }

    @Override
    public void disconnect() {
    }

    @Override
    public boolean isHealthy() {
        return Files.isDirectory(directory);
    }

    /** @return the backend's root data directory; callers may create files beneath it. */
    public Path directory() {
        return directory;
    }
}
