package com.github.frosxt.prisoncore.storage.core;

import com.github.frosxt.prisoncore.storage.api.AsyncRepository;
import com.github.frosxt.prisoncore.storage.api.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * Base class for repositories that want a write-through in-memory cache with async
 * variants. Subclasses implement the {@code loadFromBackend}, {@code saveToBackend},
 * {@code deleteFromBackend}, {@code idOf} hooks; this class handles cache lookup,
 * invalidation, and async dispatch to the supplied executor.
 */
public abstract class AbstractCachingRepository<ID, AGGREGATE> implements Repository<ID, AGGREGATE>, AsyncRepository<ID, AGGREGATE> {
    private final ConcurrentHashMap<ID, AGGREGATE> cache = new ConcurrentHashMap<>();
    private final ExecutorService executor;

    protected AbstractCachingRepository(final ExecutorService executor) {
        this.executor = executor;
    }

    protected abstract Optional<AGGREGATE> loadFromBackend(ID id);
    protected abstract Collection<AGGREGATE> loadAllFromBackend();
    protected abstract void saveToBackend(ID id, AGGREGATE aggregate);
    protected abstract void deleteFromBackend(ID id);
    protected abstract ID idOf(AGGREGATE aggregate);

    @Override
    public Optional<AGGREGATE> find(final ID id) {
        final AGGREGATE cached = cache.get(id);
        if (cached != null) {
            return Optional.of(cached);
        }
        final Optional<AGGREGATE> loaded = loadFromBackend(id);
        loaded.ifPresent(agg -> cache.put(id, agg));
        return loaded;
    }

    @Override
    public Collection<AGGREGATE> findAll() {
        if (cache.isEmpty()) {
            final Collection<AGGREGATE> all = loadAllFromBackend();
            for (final AGGREGATE agg : all) {
                cache.put(idOf(agg), agg);
            }
            return all;
        }
        return cache.values();
    }

    @Override
    public void save(final AGGREGATE aggregate) {
        final ID id = idOf(aggregate);
        cache.put(id, aggregate);
        saveToBackend(id, aggregate);
    }

    @Override
    public void delete(final ID id) {
        cache.remove(id);
        deleteFromBackend(id);
    }

    @Override
    public boolean exists(final ID id) {
        return cache.containsKey(id) || loadFromBackend(id).isPresent();
    }

    @Override
    public long count() {
        return findAll().size();
    }

    @Override
    public CompletableFuture<Optional<AGGREGATE>> findAsync(final ID id) {
        return CompletableFuture.supplyAsync(() -> find(id), executor);
    }

    @Override
    public CompletableFuture<Collection<AGGREGATE>> findAllAsync() {
        return CompletableFuture.supplyAsync(this::findAll, executor);
    }

    @Override
    public CompletableFuture<Void> saveAsync(final AGGREGATE aggregate) {
        return CompletableFuture.runAsync(() -> save(aggregate), executor);
    }

    @Override
    public CompletableFuture<Void> deleteAsync(final ID id) {
        return CompletableFuture.runAsync(() -> delete(id), executor);
    }

    @Override
    public CompletableFuture<Boolean> existsAsync(final ID id) {
        return CompletableFuture.supplyAsync(() -> exists(id), executor);
    }

    public void invalidateCache() {
        cache.clear();
    }

    public void invalidate(final ID id) {
        cache.remove(id);
    }
}
