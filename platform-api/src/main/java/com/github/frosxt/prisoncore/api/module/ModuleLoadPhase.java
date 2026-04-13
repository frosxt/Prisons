package com.github.frosxt.prisoncore.api.module;

/**
 * Controls when a module is prepared and enabled relative to kernel infrastructure.
 * Modules that contribute infrastructure (e.g. storage backends) must load
 * {@link #PRE_INFRASTRUCTURE}. Feature modules should use {@link #POST_INFRASTRUCTURE}.
 * Use {@link #LATE} only when the module depends on another module being fully enabled first.
 */
public enum ModuleLoadPhase {
    /** Loads during {@code BOOTSTRAPPING}, before infrastructure services are finalized. */
    PRE_INFRASTRUCTURE,
    /** Loads after {@code INFRASTRUCTURE_READY}. The normal choice for feature modules. */
    POST_INFRASTRUCTURE,
    /** Loads after every {@code POST_INFRASTRUCTURE} module has been enabled. */
    LATE
}
