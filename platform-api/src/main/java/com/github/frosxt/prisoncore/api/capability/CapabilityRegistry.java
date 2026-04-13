package com.github.frosxt.prisoncore.api.capability;

import java.util.Optional;
import java.util.Set;

/**
 * Publish/discover named capabilities between modules without coupling to concrete types.
 * A module resolves the registry through {@link com.github.frosxt.prisoncore.api.module.ModuleContext#capabilities()}.
 *
 * <p>Use {@link CapabilityKey} with a contract interface to advertise a service,
 * or {@link #registerMarker(String)} when the presence of a feature is all that matters.
 */
public interface CapabilityRegistry {

    /** Publish an implementation for {@code key}. Throws if the key is already registered. */
    <T> void register(CapabilityKey<T> key, T implementation);

    /** Resolve the implementation for {@code key}, throwing if none is registered. */
    <T> T resolve(CapabilityKey<T> key);

    /** Resolve the implementation for {@code key}, or empty if none is registered. */
    <T> Optional<T> resolveOptional(CapabilityKey<T> key);

    boolean has(CapabilityKey<?> key);

    Set<CapabilityKey<?>> allKeys();

    /** Publish a presence-only marker; useful when no concrete contract is involved. */
    void registerMarker(String qualifiedName);

    boolean hasMarker(String qualifiedName);

    Set<String> allMarkers();

    /** Register only if the key is absent. No-op when already registered. */
    <T> void registerIfAbsent(CapabilityKey<T> key, T implementation);

    int size();
}
