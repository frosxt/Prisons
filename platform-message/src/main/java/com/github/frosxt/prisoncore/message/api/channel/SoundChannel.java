package com.github.frosxt.prisoncore.message.api.channel;

public final class SoundChannel {

    private final boolean enabled;
    private final String value;
    private final float volume;
    private final float pitch;

    public SoundChannel(final boolean enabled, final String value,
                        final float volume, final float pitch) {
        this.enabled = enabled;
        this.value = value;
        this.volume = volume;
        this.pitch = pitch;
    }

    public boolean enabled() {
        return enabled;
    }

    public String value() {
        return value;
    }

    public float volume() {
        return volume;
    }

    public float pitch() {
        return pitch;
    }

    public static SoundChannel disabled() {
        return new SoundChannel(false, "", 1.0f, 1.0f);
    }
}
