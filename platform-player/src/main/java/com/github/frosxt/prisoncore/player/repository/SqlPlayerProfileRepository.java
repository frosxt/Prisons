package com.github.frosxt.prisoncore.player.repository;

import com.github.frosxt.prisoncore.player.api.PlayerProfile;
import com.github.frosxt.prisoncore.storage.core.AbstractCachingRepository;
import com.github.frosxt.prisoncore.storage.sql.SqlBackend;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SqlPlayerProfileRepository extends AbstractCachingRepository<UUID, PlayerProfile> {
    private static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS player_profiles ("
                    + "id VARCHAR(36) PRIMARY KEY, "
                    + "username VARCHAR(16) NOT NULL, "
                    + "first_seen BIGINT NOT NULL, "
                    + "last_seen BIGINT NOT NULL, "
                    + "metadata TEXT"
                    + ")";

    private static final String SELECT_BY_ID =
            "SELECT id, username, first_seen, last_seen, metadata FROM player_profiles WHERE id = ?";

    private static final String SELECT_ALL =
            "SELECT id, username, first_seen, last_seen, metadata FROM player_profiles";

    private static final String UPSERT =
            "INSERT INTO player_profiles (id, username, first_seen, last_seen, metadata) "
                    + "VALUES (?, ?, ?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE username = VALUES(username), "
                    + "last_seen = VALUES(last_seen), metadata = VALUES(metadata)";

    private static final String DELETE_BY_ID =
            "DELETE FROM player_profiles WHERE id = ?";

    private final SqlBackend backend;
    private final Gson gson;
    private final Logger logger;

    public SqlPlayerProfileRepository(final SqlBackend backend, final ExecutorService executor,
                                      final Logger logger) {
        super(executor);
        this.backend = backend;
        this.gson = new GsonBuilder().create();
        this.logger = logger;
        ensureTable();
    }

    private void ensureTable() {
        try (final Connection conn = backend.connection();
             final PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE)) {
            stmt.execute();
        } catch (final SQLException e) {
            logger.log(Level.SEVERE, "[PrisonCore] Failed to create player_profiles table", e);
        }
    }

    @Override
    protected Optional<PlayerProfile> loadFromBackend(final UUID id) {
        try (final Connection conn = backend.connection();
             final PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            stmt.setString(1, id.toString());
            try (final ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(fromRow(rs));
                }
            }
        } catch (final SQLException e) {
            logger.log(Level.WARNING, "[PrisonCore] Failed to load profile: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    protected Collection<PlayerProfile> loadAllFromBackend() {
        final Collection<PlayerProfile> profiles = new ArrayList<>();
        try (final Connection conn = backend.connection();
             final PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
             final ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                profiles.add(fromRow(rs));
            }
        } catch (final SQLException e) {
            logger.log(Level.WARNING, "[PrisonCore] Failed to load all profiles", e);
        }
        return profiles;
    }

    @Override
    protected void saveToBackend(final UUID id, final PlayerProfile profile) {
        try (final Connection conn = backend.connection();
             final PreparedStatement stmt = conn.prepareStatement(UPSERT)) {
            stmt.setString(1, id.toString());
            stmt.setString(2, profile.username());
            stmt.setLong(3, profile.firstSeen().toEpochMilli());
            stmt.setLong(4, profile.lastSeen().toEpochMilli());
            stmt.setString(5, profile.metadata().isEmpty() ? null : gson.toJson(profile.metadata()));
            stmt.executeUpdate();
        } catch (final SQLException e) {
            logger.log(Level.SEVERE, "[PrisonCore] Failed to save profile: " + id, e);
        }
    }

    @Override
    protected void deleteFromBackend(final UUID id) {
        try (final Connection conn = backend.connection();
             final PreparedStatement stmt = conn.prepareStatement(DELETE_BY_ID)) {
            stmt.setString(1, id.toString());
            stmt.executeUpdate();
        } catch (final SQLException e) {
            logger.log(Level.WARNING, "[PrisonCore] Failed to delete profile: " + id, e);
        }
    }

    @Override
    protected UUID idOf(final PlayerProfile profile) {
        return profile.id();
    }

    @SuppressWarnings("unchecked")
    private PlayerProfile fromRow(final ResultSet rs) throws SQLException {
        final UUID id = UUID.fromString(rs.getString("id"));
        final String username = rs.getString("username");
        final Instant firstSeen = Instant.ofEpochMilli(rs.getLong("first_seen"));
        final Instant lastSeen = Instant.ofEpochMilli(rs.getLong("last_seen"));
        final PlayerProfile profile = new PlayerProfile(id, username, firstSeen, lastSeen);

        final String metadataJson = rs.getString("metadata");
        if (metadataJson != null && !metadataJson.isEmpty()) {
            final Map<String, Object> meta = gson.fromJson(metadataJson, Map.class);
            if (meta != null) {
                meta.forEach(profile::setMetadata);
            }
        }
        return profile;
    }
}
