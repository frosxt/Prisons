package com.github.frosxt.prisoncore.api.capability;

import java.util.Optional;

public final class Capabilities {
    private Capabilities() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static <T> CapabilityKey<T> key(final String name, final Class<T> type) {
        return CapabilityKey.of(name, type);
    }

    public static <T> CapabilityKey<T> key(final String namespace, final String name, final Class<T> type) {
        return CapabilityKey.of(namespace, name, type);
    }

    public static <T> T require(final CapabilityRegistry registry, final CapabilityKey<T> key) {
        return registry.resolveOptional(key)
                .orElseThrow(() -> new IllegalStateException(
                        "Required capability not found: " + key.qualifiedName()));
    }

    public static <T> Optional<T> find(final CapabilityRegistry registry, final CapabilityKey<T> key) {
        return registry.resolveOptional(key);
    }

    public static boolean isProvided(final CapabilityRegistry registry, final String markerName) {
        return registry.hasMarker(markerName);
    }
}
