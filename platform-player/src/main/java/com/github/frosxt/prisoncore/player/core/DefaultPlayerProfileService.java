package com.github.frosxt.prisoncore.player.core;

import com.github.frosxt.prisoncore.player.api.PlayerProfile;
import com.github.frosxt.prisoncore.player.api.PlayerProfileService;
import com.github.frosxt.prisoncore.player.api.PlayerSession;
import com.github.frosxt.prisoncore.player.api.PlayerView;
import com.github.frosxt.prisoncore.storage.api.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DefaultPlayerProfileService implements PlayerProfileService, AutoCloseable {
    private final Repository<UUID, PlayerProfile> repository;
    private final Map<UUID, PlayerSession> sessions = new ConcurrentHashMap<>();
    private final List<CompletableFuture<Void>> pendingSaves = new CopyOnWriteArrayList<>();
    private final ExecutorService executor;
    private final Logger logger;

    public DefaultPlayerProfileService(final Repository<UUID, PlayerProfile> repository, final ExecutorService executor, final Logger logger) {
        this.repository = repository;
        this.executor = executor;
        this.logger = logger;
    }

    @Override
    public CompletableFuture<PlayerProfile> loadOrCreate(final UUID playerId, final String username) {
        return CompletableFuture.supplyAsync(() -> {
            final Optional<PlayerProfile> existing = repository.find(playerId);
            if (existing.isPresent()) {
                final PlayerProfile profile = existing.get();
                profile.setUsername(username);
                profile.setLastSeen(Instant.now());
                return profile;
            }
            return PlayerProfile.createNew(playerId, username);
        }, executor);
    }

    @Override
    public CompletableFuture<Void> save(final PlayerProfile profile) {
        final CompletableFuture<Void> future = CompletableFuture.runAsync(() -> repository.save(profile), executor);
        pendingSaves.add(future);
        future.whenComplete((v, ex) -> {
            pendingSaves.remove(future);
            if (ex != null) {
                logger.log(Level.WARNING, "[PrisonCore] Async save failed for " + profile.id(), ex);
            }
        });

        return future;
    }

    @Override
    public Optional<PlayerSession> getSession(final UUID playerId) {
        return Optional.ofNullable(sessions.get(playerId));
    }

    @Override
    public PlayerSession createSession(final UUID playerId, final PlayerProfile profile) {
        final PlayerSession session = new PlayerSession(playerId, profile);
        sessions.put(playerId, session);
        return session;
    }

    @Override
    public void destroySession(final UUID playerId) {
        final PlayerSession session = sessions.remove(playerId);
        if (session != null) {
            save(session.profile());
        }
    }

    @Override
    public Optional<PlayerView> getView(final UUID playerId) {
        return getSession(playerId).map(s -> new PlayerView(s.profile()));
    }

    @Override
    public void close() {
        if (!pendingSaves.isEmpty()) {
            logger.info("[PrisonCore] Waiting for " + pendingSaves.size() + " pending profile save(s)...");
            CompletableFuture.allOf(pendingSaves.toArray(new CompletableFuture[0]))
                    .orTimeout(5, TimeUnit.SECONDS)
                    .exceptionally(ex -> null)
                    .join();
        }

        final List<UUID> activeIds = new ArrayList<>(sessions.keySet());
        if (!activeIds.isEmpty()) {
            logger.info("[PrisonCore] Saving " + activeIds.size() + " active player profile(s)...");
            for (final UUID playerId : activeIds) {
                final PlayerSession session = sessions.remove(playerId);
                if (session != null) {
                    try {
                        repository.save(session.profile());
                    } catch (final Exception e) {
                        logger.log(Level.WARNING,
                                "[PrisonCore] Failed to save profile at shutdown for " + playerId, e);
                    }
                }
            }
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (final InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
