package com.github.frosxt.prisoncore.api.event.events;

import com.github.frosxt.prisoncore.api.event.DomainEvent;
import com.github.frosxt.prisoncore.api.lifecycle.LifecycleState;

public final class PlatformStateChangedEvent extends DomainEvent {
    private final LifecycleState from;
    private final LifecycleState to;

    public PlatformStateChangedEvent(final LifecycleState from, final LifecycleState to) {
        super("kernel");
        this.from = from;
        this.to = to;
    }

    public LifecycleState from() {
        return from;
    }

    public LifecycleState to() {
        return to;
    }
}
