package com.github.frosxt.prisoncore.api.event;

/**
 * Base for every event published on the {@link DomainEventBus}. Carries a source
 * tag (typically the module id that raised it) and a creation timestamp.
 */
public abstract class DomainEvent {
    private final long timestamp;
    private final String source;

    protected DomainEvent(final String source) {
        this.timestamp = System.currentTimeMillis();
        this.source = source;
    }

    public long timestamp() {
        return timestamp;
    }

    public String source() {
        return source;
    }
}
