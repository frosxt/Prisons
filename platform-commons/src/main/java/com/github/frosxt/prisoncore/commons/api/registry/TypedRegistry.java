package com.github.frosxt.prisoncore.commons.api.registry;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface TypedRegistry<K, V> {
    void register(K key, V value);
    Optional<V> get(K key);
    V getOrThrow(K key);
    boolean has(K key);
    void unregister(K key);
    Set<K> keys();
    Collection<V> values();
    int size();
}
