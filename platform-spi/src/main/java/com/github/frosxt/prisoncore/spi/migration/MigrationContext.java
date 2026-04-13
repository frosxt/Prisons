package com.github.frosxt.prisoncore.spi.migration;

import java.util.logging.Logger;

public interface MigrationContext {
    Logger logger();
    void executeSql(String sql) throws Exception;
    <T> T storage(Class<T> storageType);
}
