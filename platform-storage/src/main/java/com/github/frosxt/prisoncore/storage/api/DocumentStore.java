package com.github.frosxt.prisoncore.storage.api;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Schemaless document store. Lighter than {@link Repository} — no {@code count()},
 * no {@code exists()}, supports arbitrary {@link Predicate} filtering over the values.
 *
 * @param <ID> document identifier
 * @param <D> document value type
 */
public interface DocumentStore<ID, D> {
    Optional<D> find(ID id);
    Collection<D> findAll();
    Collection<D> findWhere(Predicate<D> filter);
    void save(ID id, D document);
    void delete(ID id);
}
