package com.github.frosxt.prisoncore.player.repository;

import com.github.frosxt.prisoncore.player.api.PlayerProfile;
import com.github.frosxt.prisoncore.storage.api.Repository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class JsonPlayerProfileRepository implements Repository<UUID, PlayerProfile> {
    private final Path directory;
    private final Gson gson;
    private final Logger logger;

    public JsonPlayerProfileRepository(final Path directory, final Logger logger) {
        this.directory = directory;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.logger = logger;
        try {
            Files.createDirectories(directory);
        } catch (final IOException e) {
            logger.log(Level.SEVERE, "Failed to create player data directory", e);
        }
    }

    @Override
    public Optional<PlayerProfile> find(final UUID id) {
        final Path file = directory.resolve(id + ".json");
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        try (final Reader reader = Files.newBufferedReader(file)) {
            final JsonObject json = gson.fromJson(reader, JsonObject.class);
            return Optional.of(fromJson(json));
        } catch (final Exception e) {
            logger.log(Level.WARNING, "Failed to load player profile: " + id, e);
            return Optional.empty();
        }
    }

    @Override
    public Collection<PlayerProfile> findAll() {
        final List<PlayerProfile> profiles = new ArrayList<>();
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*.json")) {
            for (final Path file : stream) {
                try (final Reader reader = Files.newBufferedReader(file)) {
                    final JsonObject json = gson.fromJson(reader, JsonObject.class);
                    profiles.add(fromJson(json));
                }
            }
        } catch (final IOException e) {
            logger.log(Level.WARNING, "Failed to list player profiles", e);
        }
        return profiles;
    }

    @Override
    public void save(final PlayerProfile profile) {
        final Path file = directory.resolve(profile.id() + ".json");
        final Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        try (final Writer writer = Files.newBufferedWriter(temp)) {
            gson.toJson(toJson(profile), writer);
        } catch (final IOException e) {
            logger.log(Level.SEVERE, "Failed to save player profile: " + profile.id(), e);
            return;
        }
        try {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (final IOException e) {
            try {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            } catch (final IOException ex) {
                logger.log(Level.SEVERE, "Failed to finalize profile save: " + profile.id(), ex);
            }
        }
    }

    @Override
    public void delete(final UUID id) {
        try {
            Files.deleteIfExists(directory.resolve(id + ".json"));
        } catch (final IOException e) {
            logger.log(Level.WARNING, "Failed to delete player profile: " + id, e);
        }
    }

    @Override
    public boolean exists(final UUID id) {
        return Files.exists(directory.resolve(id + ".json"));
    }

    @Override
    public long count() {
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*.json")) {
            long count = 0;
            for (final Path ignored : stream) {
                count++;
            }
            return count;
        } catch (final IOException e) {
            return 0;
        }
    }

    private JsonObject toJson(final PlayerProfile profile) {
        final JsonObject json = new JsonObject();
        json.addProperty("id", profile.id().toString());
        json.addProperty("username", profile.username());
        json.addProperty("firstSeen", profile.firstSeen().toString());
        json.addProperty("lastSeen", profile.lastSeen().toString());

        if (!profile.metadata().isEmpty()) {
            json.add("metadata", gson.toJsonTree(profile.metadata()));
        }
        return json;
    }

    @SuppressWarnings("unchecked")
    private PlayerProfile fromJson(final JsonObject json) {
        final UUID id = UUID.fromString(json.get("id").getAsString());
        final String username = json.get("username").getAsString();
        final Instant firstSeen = Instant.parse(json.get("firstSeen").getAsString());
        final Instant lastSeen = Instant.parse(json.get("lastSeen").getAsString());
        final PlayerProfile profile = new PlayerProfile(id, username, firstSeen, lastSeen);

        if (json.has("metadata")) {
            final Map<String, Object> meta = gson.fromJson(json.get("metadata"), Map.class);
            if (meta != null) {
                meta.forEach(profile::setMetadata);
            }
        }
        return profile;
    }
}
