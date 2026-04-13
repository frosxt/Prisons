package com.github.frosxt.prisoncore.commons.core.util;

import java.util.concurrent.ThreadLocalRandom;

public final class MathUtil {
    private MathUtil() {
        throw new UnsupportedOperationException("Utility classes cannot be instantiated");
    }

    public static int clamp(final int value, final int min, final int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double clamp(final double value, final double min, final double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int randomInt(final int min, final int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static double randomDouble(final double min, final double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    public static boolean chance(final double percentage) {
        return ThreadLocalRandom.current().nextDouble(100.0) < percentage;
    }
}
