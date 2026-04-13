package com.github.frosxt.prisoncore.storage.mongo;

import com.github.frosxt.prisoncore.spi.storage.StorageBackend;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.util.Map;
import java.util.logging.Logger;

/**
 * MongoDB storage backend. Modules retrieve the connected database handle via
 * {@link #database()} and use the Mongo Java driver directly for queries.
 */
public final class MongoBackend implements StorageBackend {
    private final Map<String, String> config;
    private final Logger logger;
    private MongoClient client;
    private MongoDatabase database;

    public MongoBackend(final Map<String, String> config, final Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    @Override
    public String name() {
        return "mongo";
    }

    @Override
    public void connect() throws Exception {
        final String uri = config.getOrDefault("uri", "mongodb://localhost:27017");
        final String dbName = config.getOrDefault("database", "prisoncore");
        client = MongoClients.create(uri);
        database = client.getDatabase(dbName);
        logger.info("[PrisonCore] MongoDB connected to database: " + dbName);
    }

    @Override
    public void disconnect() {
        if (client != null) {
            client.close();
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            if (database == null) {
                return false;
            }
            database.listCollectionNames().first();
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

    /** @return the connected Mongo database; valid between {@code connect()} and {@code disconnect()}. */
    public MongoDatabase database() {
        return database;
    }
}
