package com.github.frosxt.prisoncore.storage.api;

import java.util.Optional;
import java.util.Set;

/**
 * Simple key/value contract. Use when you don't need aggregate queries, counts,
 * or predicate filtering — just {@code get}, {@code put}, {@code remove}.
 */
public interface KeyValueStore<K, V> {
    Optional<V> get(K key);
    void put(K key, V value);
    void remove(K key);
    boolean contains(K key);
    Set<K> keys();
}
