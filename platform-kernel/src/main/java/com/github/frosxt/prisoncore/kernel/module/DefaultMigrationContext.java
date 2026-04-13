package com.github.frosxt.prisoncore.kernel.module;

import com.github.frosxt.prisoncore.api.service.ServiceContainer;
import com.github.frosxt.prisoncore.spi.migration.MigrationContext;

import java.util.logging.Logger;

public final class DefaultMigrationContext implements MigrationContext {
    private final Logger logger;
    private final ServiceContainer container;

    public DefaultMigrationContext(final Logger logger, final ServiceContainer container) {
        this.logger = logger;
        this.container = container;
    }

    @Override
    public Logger logger() {
        return logger;
    }

    @Override
    public void executeSql(final String sql) throws Exception {
        throw new UnsupportedOperationException(
                "Direct SQL execution requires a SQL storage backend. " +
                "Use storage(SqlBackend.class) to get a connection.");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T storage(final Class<T> storageType) {
        return container.resolve(storageType);
    }
}
