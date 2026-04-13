package com.github.frosxt.prisoncore.storage.api;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Async variant of {@link Repository}. Callers should prefer this shape for any
 * repository operation invoked from the main thread.
 *
 * @param <ID> aggregate identifier type
 * @param <AGGREGATE> aggregate value type
 */
public interface AsyncRepository<ID, AGGREGATE> {
    CompletableFuture<Optional<AGGREGATE>> findAsync(ID id);
    CompletableFuture<Collection<AGGREGATE>> findAllAsync();
    CompletableFuture<Void> saveAsync(AGGREGATE aggregate);
    CompletableFuture<Void> deleteAsync(ID id);
    CompletableFuture<Boolean> existsAsync(ID id);
}
