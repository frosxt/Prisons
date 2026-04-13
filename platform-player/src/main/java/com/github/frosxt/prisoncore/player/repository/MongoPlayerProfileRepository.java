package com.github.frosxt.prisoncore.player.repository;

import com.github.frosxt.prisoncore.player.api.PlayerProfile;
import com.github.frosxt.prisoncore.storage.core.AbstractCachingRepository;
import com.github.frosxt.prisoncore.storage.mongo.MongoBackend;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MongoPlayerProfileRepository extends AbstractCachingRepository<UUID, PlayerProfile> {
    private static final String COLLECTION_NAME = "player_profiles";

    private final MongoBackend backend;
    private final Gson gson;
    private final Logger logger;

    public MongoPlayerProfileRepository(final MongoBackend backend, final ExecutorService executor,
                                        final Logger logger) {
        super(executor);
        this.backend = backend;
        this.gson = new GsonBuilder().create();
        this.logger = logger;
    }

    private MongoCollection<Document> collection() {
        return backend.database().getCollection(COLLECTION_NAME);
    }

    @Override
    protected Optional<PlayerProfile> loadFromBackend(final UUID id) {
        try {
            final Document doc = collection().find(Filters.eq("_id", id.toString())).first();
            if (doc == null) {
                return Optional.empty();
            }
            return Optional.of(fromDocument(doc));
        } catch (final Exception e) {
            logger.log(Level.WARNING, "[PrisonCore] Failed to load profile: " + id, e);
            return Optional.empty();
        }
    }

    @Override
    protected Collection<PlayerProfile> loadAllFromBackend() {
        final Collection<PlayerProfile> profiles = new ArrayList<>();
        try {
            for (final Document doc : collection().find()) {
                profiles.add(fromDocument(doc));
            }
        } catch (final Exception e) {
            logger.log(Level.WARNING, "[PrisonCore] Failed to load all profiles", e);
        }
        return profiles;
    }

    @Override
    protected void saveToBackend(final UUID id, final PlayerProfile profile) {
        try {
            final Document doc = toDocument(profile);
            collection().replaceOne(
                    Filters.eq("_id", id.toString()),
                    doc,
                    new ReplaceOptions().upsert(true)
            );
        } catch (final Exception e) {
            logger.log(Level.SEVERE, "[PrisonCore] Failed to save profile: " + id, e);
        }
    }

    @Override
    protected void deleteFromBackend(final UUID id) {
        try {
            collection().deleteOne(Filters.eq("_id", id.toString()));
        } catch (final Exception e) {
            logger.log(Level.WARNING, "[PrisonCore] Failed to delete profile: " + id, e);
        }
    }

    @Override
    protected UUID idOf(final PlayerProfile profile) {
        return profile.id();
    }

    private Document toDocument(final PlayerProfile profile) {
        final Document doc = new Document();
        doc.put("_id", profile.id().toString());
        doc.put("username", profile.username());
        doc.put("first_seen", profile.firstSeen().toEpochMilli());
        doc.put("last_seen", profile.lastSeen().toEpochMilli());
        if (!profile.metadata().isEmpty()) {
            doc.put("metadata", Document.parse(gson.toJson(profile.metadata())));
        }
        return doc;
    }

    @SuppressWarnings("unchecked")
    private PlayerProfile fromDocument(final Document doc) {
        final UUID id = UUID.fromString(doc.getString("_id"));
        final String username = doc.getString("username");
        final Instant firstSeen = Instant.ofEpochMilli(doc.getLong("first_seen"));
        final Instant lastSeen = Instant.ofEpochMilli(doc.getLong("last_seen"));
        final PlayerProfile profile = new PlayerProfile(id, username, firstSeen, lastSeen);

        final Document metaDoc = doc.get("metadata", Document.class);
        if (metaDoc != null) {
            final Map<String, Object> meta = gson.fromJson(metaDoc.toJson(), Map.class);
            if (meta != null) {
                meta.forEach(profile::setMetadata);
            }
        }
        return profile;
    }
}
