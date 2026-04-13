package com.github.frosxt.prisoncore.api.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ServiceDescriptor<T> {
    private final Class<T> type;
    private final Function<ServiceContainer, T> factory;
    private final ServiceScope scope;
    private final Set<Class<?>> dependencies;
    private final String name;
    private final Consumer<T> initCallback;
    private final Consumer<T> destroyCallback;

    private ServiceDescriptor(final Builder<T> builder) {
        this.type = Objects.requireNonNull(builder.type, "type");
        this.factory = Objects.requireNonNull(builder.factory, "factory");
        this.scope = builder.scope;
        this.dependencies = Set.copyOf(builder.dependencies);
        this.name = builder.name;
        this.initCallback = builder.initCallback;
        this.destroyCallback = builder.destroyCallback;
    }

    public static <T> Builder<T> builder(final Class<T> type) {
        return new Builder<>(type);
    }

    public Class<T> type() {
        return type;
    }

    public Function<ServiceContainer, T> factory() {
        return factory;
    }

    public ServiceScope scope() {
        return scope;
    }

    public Set<Class<?>> dependencies() {
        return dependencies;
    }

    public String name() {
        return name;
    }

    public Consumer<T> initCallback() {
        return initCallback;
    }

    public Consumer<T> destroyCallback() {
        return destroyCallback;
    }

    public static final class Builder<T> {
        private final Class<T> type;
        private Function<ServiceContainer, T> factory;
        private ServiceScope scope = ServiceScope.KERNEL;
        private final Set<Class<?>> dependencies = new HashSet<>();
        private String name;
        private Consumer<T> initCallback;
        private Consumer<T> destroyCallback;

        private Builder(final Class<T> type) {
            this.type = type;
        }

        public Builder<T> factory(final Function<ServiceContainer, T> factory) {
            this.factory = factory;
            return this;
        }

        public Builder<T> instance(final T instance) {
            this.factory = container -> instance;
            return this;
        }

        public Builder<T> scope(final ServiceScope scope) {
            this.scope = scope;
            return this;
        }

        public Builder<T> dependsOn(final Class<?>... types) {
            Collections.addAll(this.dependencies, types);
            return this;
        }

        public Builder<T> name(final String name) {
            this.name = name;
            return this;
        }

        public Builder<T> onInitialize(final Consumer<T> callback) {
            this.initCallback = callback;
            return this;
        }

        public Builder<T> onDestroy(final Consumer<T> callback) {
            this.destroyCallback = callback;
            return this;
        }

        public ServiceDescriptor<T> build() {
            return new ServiceDescriptor<>(this);
        }
    }
}
