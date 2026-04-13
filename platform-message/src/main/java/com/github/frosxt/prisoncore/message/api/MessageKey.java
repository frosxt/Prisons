package com.github.frosxt.prisoncore.message.api;

import java.util.Objects;

/**
 * Identifier for a catalog-backed message. The qualified form is
 * {@code namespace-key} — used as the YAML key in {@code messages.yml}.
 */
public final class MessageKey {

    private final String namespace;
    private final String key;

    private MessageKey(final String namespace, final String key) {
        this.namespace = Objects.requireNonNull(namespace, "namespace");
        this.key = Objects.requireNonNull(key, "key");
    }

    public static MessageKey of(final String namespace, final String key) {
        return new MessageKey(namespace, key);
    }

    public String namespace() {
        return namespace;
    }

    public String key() {
        return key;
    }

    public String qualifiedKey() {
        return namespace + "-" + key;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof final MessageKey that)) {
            return false;
        }
        return namespace.equals(that.namespace) && key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, key);
    }

    @Override
    public String toString() {
        return qualifiedKey();
    }
}
