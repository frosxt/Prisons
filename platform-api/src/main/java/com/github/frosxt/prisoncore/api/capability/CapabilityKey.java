package com.github.frosxt.prisoncore.api.capability;

import java.util.Objects;

/**
 * Typed, namespaced identifier for a capability registered in {@link CapabilityRegistry}.
 * Two keys are equal when their namespace and name match; the contract type is carried
 * for type-safe lookup but does not participate in equality.
 *
 * @param <T> the contract type a provider must satisfy
 */
public final class CapabilityKey<T> implements Comparable<CapabilityKey<?>> {
    private final String namespace;
    private final String name;
    private final Class<T> contractType;

    private CapabilityKey(final String namespace, final String name, final Class<T> contractType) {
        this.namespace = Objects.requireNonNull(namespace, "namespace");
        this.name = Objects.requireNonNull(name, "name");
        this.contractType = Objects.requireNonNull(contractType, "contractType");
    }

    public static <T> CapabilityKey<T> of(final String namespace, final String name, final Class<T> contractType) {
        return new CapabilityKey<>(namespace, name, contractType);
    }

    public static <T> CapabilityKey<T> of(final String name, final Class<T> contractType) {
        return new CapabilityKey<>("core", name, contractType);
    }

    public String namespace() {
        return namespace;
    }

    public String name() {
        return name;
    }

    public Class<T> contractType() {
        return contractType;
    }

    public String qualifiedName() {
        return namespace + ":" + name;
    }

    @Override
    public int compareTo(final CapabilityKey<?> other) {
        return this.qualifiedName().compareTo(other.qualifiedName());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof final CapabilityKey<?> that)) {
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
