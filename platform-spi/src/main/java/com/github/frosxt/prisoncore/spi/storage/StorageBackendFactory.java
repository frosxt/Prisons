package com.github.frosxt.prisoncore.spi.storage;

import java.util.Map;

/**
 * Factory for {@link StorageBackend} instances. Register concrete factories with
 * {@link com.github.frosxt.prisoncore.kernel.storage.StorageRegistry#registerFactory}
 * during {@code PRE_INFRASTRUCTURE} module load to contribute new backend types.
 */
public interface StorageBackendFactory {
    /** @return the type identifier users select via {@code core.yml storage.backend}. */
    String type();
    StorageBackend create(Map<String, Object> config);
}
