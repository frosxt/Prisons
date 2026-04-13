package com.github.frosxt.prisoncore.storage.api;

import java.util.List;

/**
 * Append-only log of entries grouped by {@code K}. Use for transaction journals,
 * audit trails, or event streams where records must not be mutated once written.
 */
public interface LedgerStore<K, E> {
    void append(K key, E entry);
    List<E> entries(K key);
    /** @return the most recent {@code limit} entries for {@code key}, newest first. */
    List<E> entries(K key, int limit);
}
