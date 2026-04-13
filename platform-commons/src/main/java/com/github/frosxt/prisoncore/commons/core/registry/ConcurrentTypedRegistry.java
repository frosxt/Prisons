package com.github.frosxt.prisoncore.commons.core.registry;

import com.github.frosxt.prisoncore.commons.api.registry.TypedRegistry;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ConcurrentTypedRegistry<K, V> implements TypedRegistry<K, V> {
    private final Map<K, V> entries = new ConcurrentHashMap<>();

    @Override
    public void register(final K key, final V value) {
        entries.put(key, value);
    }

    @Override
    public Optional<V> get(final K key) {
        return Optional.ofNullable(entries.get(key));
    }

    @Override
    public V getOrThrow(final K key) {
        return get(key).orElseThrow(() -> new NoSuchElementException("No entry for key: " + key));
    }

    @Override
    public boolean has(final K key) {
        return entries.containsKey(key);
    }

    @Override
    public void unregister(final K key) {
        entries.remove(key);
    }

    @Override
    public Set<K> keys() {
        return Collections.unmodifiableSet(entries.keySet());
    }

    @Override
    public Collection<V> values() {
        return Collections.unmodifiableCollection(entries.values());
    }

    @Override
    public int size() {
        return entries.size();
    }
}
