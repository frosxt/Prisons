package com.github.frosxt.prisoncore.message.api.channel;

import java.util.Collections;
import java.util.List;

public final class ChatChannel {

    private final boolean enabled;
    private final List<String> lines;

    public ChatChannel(final boolean enabled, final List<String> lines) {
        this.enabled = enabled;
        this.lines = Collections.unmodifiableList(lines);
    }

    public boolean enabled() {
        return enabled;
    }

    public List<String> lines() {
        return lines;
    }

    public static ChatChannel disabled() {
        return new ChatChannel(false, Collections.emptyList());
    }
}
