package com.github.frosxt.prisoncore.storage.api;

import java.util.Collection;
import java.util.Optional;

/**
 * Synchronous aggregate repository. Implementations back each method with a specific
 * storage technology (Json, Sqlite, Sql, Mongo). For async access, use {@link AsyncRepository}.
 *
 * @param <ID> aggregate identifier type
 * @param <AGGREGATE> aggregate value type
 */
public interface Repository<ID, AGGREGATE> {
    Optional<AGGREGATE> find(ID id);
    Collection<AGGREGATE> findAll();
    void save(AGGREGATE aggregate);
    void delete(ID id);
    boolean exists(ID id);
    long count();
}
