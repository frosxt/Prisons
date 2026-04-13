package com.github.frosxt.prisoncore.commons.bukkit.item;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;

/**
 * Typed read/write helpers over a {@link PersistentDataContainer}. All keys are
 * created under a shared namespace set once at boot via {@link #setNamespace(String)}.
 */
public final class PersistentDataAdapter {
    private static String namespace;

    private PersistentDataAdapter() {
        throw new UnsupportedOperationException("Utility classes cannot be instantiated");
    }

    public static void setNamespace(final String namespace) {
        PersistentDataAdapter.namespace = namespace;
    }

    public static void setString(final PersistentDataContainer pdc, final String key, final String value) {
        pdc.set(createKey(key), PersistentDataType.STRING, value);
    }

    public static Optional<String> getString(final PersistentDataContainer pdc, final String key) {
        return Optional.ofNullable(pdc.get(createKey(key), PersistentDataType.STRING));
    }

    public static void setInt(final PersistentDataContainer pdc, final String key, final int value) {
        pdc.set(createKey(key), PersistentDataType.INTEGER, value);
    }

    public static Optional<Integer> getInt(final PersistentDataContainer pdc, final String key) {
        return Optional.ofNullable(pdc.get(createKey(key), PersistentDataType.INTEGER));
    }

    public static void setLong(final PersistentDataContainer pdc, final String key, final long value) {
        pdc.set(createKey(key), PersistentDataType.LONG, value);
    }

    public static Optional<Long> getLong(final PersistentDataContainer pdc, final String key) {
        return Optional.ofNullable(pdc.get(createKey(key), PersistentDataType.LONG));
    }

    public static void setDouble(final PersistentDataContainer pdc, final String key, final double value) {
        pdc.set(createKey(key), PersistentDataType.DOUBLE, value);
    }

    public static Optional<Double> getDouble(final PersistentDataContainer pdc, final String key) {
        return Optional.ofNullable(pdc.get(createKey(key), PersistentDataType.DOUBLE));
    }

    public static void setBoolean(final PersistentDataContainer pdc, final String key, final boolean value) {
        pdc.set(createKey(key), PersistentDataType.BYTE, (byte) (value ? 1 : 0));
    }

    public static Optional<Boolean> getBoolean(final PersistentDataContainer pdc, final String key) {
        final Byte raw = pdc.get(createKey(key), PersistentDataType.BYTE);
        if (raw == null) {
            return Optional.empty();
        }
        return Optional.of(raw != 0);
    }

    public static boolean hasKey(final PersistentDataContainer pdc, final String key) {
        final NamespacedKey namespacedKey = createKey(key);
        return pdc.has(namespacedKey, PersistentDataType.STRING)
                || pdc.has(namespacedKey, PersistentDataType.INTEGER)
                || pdc.has(namespacedKey, PersistentDataType.LONG)
                || pdc.has(namespacedKey, PersistentDataType.DOUBLE)
                || pdc.has(namespacedKey, PersistentDataType.BYTE);
    }

    public static void removeKey(final PersistentDataContainer pdc, final String key) {
        pdc.remove(createKey(key));
    }

    private static NamespacedKey createKey(final String key) {
        if (namespace == null) {
            throw new IllegalStateException("PersistentDataAdapter namespace has not been set. Call setNamespace() first.");
        }
        return new NamespacedKey(namespace, key);
    }
}
