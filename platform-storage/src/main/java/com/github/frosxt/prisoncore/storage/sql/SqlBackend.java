package com.github.frosxt.prisoncore.storage.sql;

import com.github.frosxt.prisoncore.spi.storage.StorageBackend;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * SQL (MySQL/MariaDB) storage backend backed by a HikariCP pool.
 * Modules call {@link #connection()} to borrow a pooled connection; callers are
 * responsible for returning it by calling {@code close()} on the borrowed handle.
 */
public final class SqlBackend implements StorageBackend {
    private final Map<String, String> config;
    private final Logger logger;
    private HikariDataSource dataSource;

    public SqlBackend(final Map<String, String> config, final Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    @Override
    public String name() {
        return "sql";
    }

    @Override
    public void connect() throws Exception {
        final HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl(config.getOrDefault("url", "jdbc:mysql://localhost:3306/prisoncore"));
        hikari.setUsername(config.getOrDefault("username", "root"));
        hikari.setPassword(config.getOrDefault("password", ""));
        hikari.setMaximumPoolSize(Integer.parseInt(config.getOrDefault("pool-size", "10")));
        hikari.setPoolName("PrisonCore-SQL");
        hikari.addDataSourceProperty("cachePrepStmts", "true");
        hikari.addDataSourceProperty("prepStmtCacheSize", "250");
        hikari.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        dataSource = new HikariDataSource(hikari);
        logger.info("[PrisonCore] SQL connection pool established.");
    }

    @Override
    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Override
    public boolean isHealthy() {
        return dataSource != null && !dataSource.isClosed();
    }

    /** @return a pooled connection; the caller must {@code close()} it to return it to the pool. */
    public Connection connection() throws SQLException {
        return dataSource.getConnection();
    }
}
