package com.github.frosxt.prisoncore.kernel.capability;

import com.github.frosxt.prisoncore.api.capability.CapabilityKey;
import com.github.frosxt.prisoncore.api.capability.CapabilityRegistry;
import com.github.frosxt.prisoncore.api.service.ServiceException;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultCapabilityRegistry implements CapabilityRegistry {
    private final ConcurrentHashMap<CapabilityKey<?>, Object> capabilities = new ConcurrentHashMap<>();

    @Override
    public <T> void register(final CapabilityKey<T> key, final T implementation) {
        if (capabilities.putIfAbsent(key, implementation) != null) {
            throw new ServiceException("Capability already registered: " + key);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T resolve(final CapabilityKey<T> key) {
        final Object impl = capabilities.get(key);
        if (impl == null) {
            throw new ServiceException("No capability registered for: " + key);
        }
        return (T) impl;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> resolveOptional(final CapabilityKey<T> key) {
        return Optional.ofNullable((T) capabilities.get(key));
    }

    @Override
    public boolean has(final CapabilityKey<?> key) {
        return capabilities.containsKey(key);
    }

    @Override
    public Set<CapabilityKey<?>> allKeys() {
        return Collections.unmodifiableSet(capabilities.keySet());
    }

    private final Set<String> markers = ConcurrentHashMap.newKeySet();

    @Override
    public void registerMarker(final String qualifiedName) {
        markers.add(qualifiedName);
    }

    @Override
    public boolean hasMarker(final String qualifiedName) {
        return markers.contains(qualifiedName);
    }

    @Override
    public Set<String> allMarkers() {
        return Collections.unmodifiableSet(markers);
    }

    @Override
    public <T> void registerIfAbsent(final CapabilityKey<T> key, final T implementation) {
        capabilities.putIfAbsent(key, implementation);
    }

    @Override
    public int size() {
        return capabilities.size() + markers.size();
    }
}
