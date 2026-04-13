package com.github.frosxt.prisoncore.commons.core.util;

public final class Preconditions {
    private Preconditions() {
        throw new UnsupportedOperationException("Utility classes cannot be instantiated");
    }

    public static <T> T requireNonNull(final T obj, final String name) {
        if (obj == null) {
            throw new NullPointerException(name + " must not be null");
        }
        return obj;
    }

    public static int requirePositive(final int value, final String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive, got: " + value);
        }
        return value;
    }

    public static int requireNonNegative(final int value, final String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " must not be negative, got: " + value);
        }
        return value;
    }

    public static String requireNotEmpty(final String value, final String name) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        return value;
    }
}
