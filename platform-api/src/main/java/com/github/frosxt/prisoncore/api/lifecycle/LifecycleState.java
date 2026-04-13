package com.github.frosxt.prisoncore.api.lifecycle;

public enum LifecycleState {
    CREATED,
    BOOTSTRAPPING,
    INFRASTRUCTURE_READY,
    MODULES_DISCOVERED,
    MODULES_RESOLVED,
    MODULES_PREPARED,
    MODULES_ENABLED,
    ACTIVE,
    QUIESCING,
    DISABLED;

    public boolean isRunning() {
        return this == ACTIVE;
    }

    public boolean isShuttingDown() {
        return this == QUIESCING || this == DISABLED;
    }

    public boolean isBeforeActive() {
        return ordinal() < ACTIVE.ordinal();
    }
}
