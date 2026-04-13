package com.github.frosxt.prisoncore.placeholder.bridge;

public final class PapiBridge {
    private PapiBridge() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isAvailable() {
        try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            return true;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }

    public static PapiPlaceholderResolver createResolver() {
        return new PapiPlaceholderResolver();
    }
}
