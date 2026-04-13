package com.github.frosxt.prisoncore.message.api.channel;

public final class ActionBarChannel {

    private final boolean enabled;
    private final String value;

    public ActionBarChannel(final boolean enabled, final String value) {
        this.enabled = enabled;
        this.value = value;
    }

    public boolean enabled() {
        return enabled;
    }

    public String value() {
        return value;
    }

    public static ActionBarChannel disabled() {
        return new ActionBarChannel(false, "");
    }
}
