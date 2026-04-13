package com.github.frosxt.prisoncore.command.api;

import java.util.Objects;

public final class CommandKey {
    private final String namespace;
    private final String name;

    public CommandKey(final String namespace, final String name) {
        this.namespace = Objects.requireNonNull(namespace);
        this.name = Objects.requireNonNull(name);
    }

    public String namespace() {
        return namespace;
    }

    public String name() {
        return name;
    }

    public String qualifiedName() {
        return namespace + ":" + name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof final CommandKey that)) {
            return false;
        }
        return namespace.equals(that.namespace) && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, name);
    }

    @Override
    public String toString() {
        return qualifiedName();
    }
}
