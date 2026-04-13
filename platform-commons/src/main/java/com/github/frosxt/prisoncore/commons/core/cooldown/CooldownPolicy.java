package com.github.frosxt.prisoncore.commons.core.cooldown;

import java.time.Duration;

public final class CooldownPolicy {
    private final Duration duration;
    private final String messageKey;

    public CooldownPolicy(final Duration duration, final String messageKey) {
        this.duration = duration;
        this.messageKey = messageKey;
    }

    public Duration duration() {
        return duration;
    }

    public String messageKey() {
        return messageKey;
    }
}
