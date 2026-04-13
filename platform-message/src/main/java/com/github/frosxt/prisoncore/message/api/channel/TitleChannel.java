package com.github.frosxt.prisoncore.message.api.channel;

public final class TitleChannel {

    private final boolean enabled;
    private final String title;
    private final String subtitle;
    private final int fadeIn;
    private final int stay;
    private final int fadeOut;

    public TitleChannel(final boolean enabled, final String title, final String subtitle,
                        final int fadeIn, final int stay, final int fadeOut) {
        this.enabled = enabled;
        this.title = title;
        this.subtitle = subtitle;
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    public boolean enabled() {
        return enabled;
    }

    public String title() {
        return title;
    }

    public String subtitle() {
        return subtitle;
    }

    public int fadeIn() {
        return fadeIn;
    }

    public int stay() {
        return stay;
    }

    public int fadeOut() {
        return fadeOut;
    }

    public static TitleChannel disabled() {
        return new TitleChannel(false, "", "", 10, 70, 20);
    }
}
