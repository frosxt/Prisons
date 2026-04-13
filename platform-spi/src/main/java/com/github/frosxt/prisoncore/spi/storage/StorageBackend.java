package com.github.frosxt.prisoncore.spi.storage;

/**
 * Minimal contract every storage backend implementation must honour.
 * Lifecycle ({@code connect} / {@code disconnect}) is driven by
 * {@link com.github.frosxt.prisoncore.kernel.storage.StorageRegistry}.
 * Concrete subclasses (JsonStorageBackend, SqliteBackend, SqlBackend, MongoBackend)
 * expose typed accessors for their underlying handle.
 */
public interface StorageBackend {
    String name();
    void connect() throws Exception;
    void disconnect();
    boolean isHealthy();
}
