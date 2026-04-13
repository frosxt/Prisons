package com.github.frosxt.prisoncore.player.api;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Central access to persisted {@link PlayerProfile} data and live {@link PlayerSession}s.
 * Modules should prefer this over rolling their own profile persistence.
 */
public interface PlayerProfileService {

    /** Load the profile for {@code playerId} asynchronously, creating a fresh one if absent. */
    CompletableFuture<PlayerProfile> loadOrCreate(UUID playerId, String username);

    /** Persist a profile asynchronously. Safe to call from the main thread. */
    CompletableFuture<Void> save(PlayerProfile profile);

    /** @return the session for an online player, empty if not online. */
    Optional<PlayerSession> getSession(UUID playerId);

    PlayerSession createSession(UUID playerId, PlayerProfile profile);

    void destroySession(UUID playerId);

    /** @return an immutable view of the player's current profile state, if online. */
    Optional<PlayerView> getView(UUID playerId);
}
