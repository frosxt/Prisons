package com.github.frosxt.prisoncore.storage.sqlite;

import com.github.frosxt.prisoncore.spi.storage.StorageBackend;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SQLite-backed storage backend. Owns a single JDBC connection to a file-based database.
 * Modules acquire the backend via the storage registry and call {@link #connection()} to
 * issue prepared statements against their own schema.
 */
public final class SqliteBackend implements StorageBackend {
    private final Path dbFile;
    private final Logger logger;
    private Connection connection;

    public SqliteBackend(final Path dbFile, final Logger logger) {
        this.dbFile = dbFile;
        this.logger = logger;
    }

    @Override
    public String name() {
        return "sqlite";
    }

    @Override
    public void connect() throws Exception {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.toAbsolutePath());
        logger.info("[PrisonCore] SQLite connected: " + dbFile.getFileName());
    }

    @Override
    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (final SQLException e) {
                logger.log(Level.WARNING, "Failed to close SQLite connection", e);
            }
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            return connection != null && !connection.isClosed();
        } catch (final SQLException e) {
            return false;
        }
    }

    /** @return the single JDBC connection owned by this backend. Do not close it. */
    public Connection connection() {
        return connection;
    }
}
