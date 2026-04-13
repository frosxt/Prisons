package com.github.frosxt.prisoncore.api.service;

import com.github.frosxt.prisoncore.api.module.ModuleHandle;

import java.util.Optional;

/**
 * The kernel's service container. Modules resolve platform services through their
 * {@link com.github.frosxt.prisoncore.api.module.ModuleContext#services()} handle.
 *
 * <p>Service lookup is type-keyed by default; use {@link #resolveByName} when multiple
 * implementations of the same interface coexist. Module-scoped instances live in a
 * {@link Scope} that is closed deterministically when the owning module disables.
 */
public interface ServiceContainer {

    /** Resolve a service by type, throwing if none is registered. */
    <T> T resolve(Class<T> type);

    <T> Optional<T> resolveOptional(Class<T> type);

    <T> Optional<T> resolveByName(String name, Class<T> type);

    boolean has(Class<?> type);

    <T> Provider<T> provider(Class<T> type);

    <T> void register(ServiceDescriptor<T> descriptor);

    /** Create a child scope whose services are disposed when the module disables. */
    Scope createModuleScope(ModuleHandle moduleHandle);

    /** A module-owned child container. Closing it releases every service it owns. */
    interface Scope extends ServiceContainer, AutoCloseable {
        ModuleHandle owner();

        @Override
        void close();
    }
}
