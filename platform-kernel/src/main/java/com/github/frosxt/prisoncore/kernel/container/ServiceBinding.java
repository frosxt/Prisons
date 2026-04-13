package com.github.frosxt.prisoncore.kernel.container;

import com.github.frosxt.prisoncore.api.service.ServiceDescriptor;
import com.github.frosxt.prisoncore.api.service.ServiceScope;

final class ServiceBinding<T> {
    private final ServiceDescriptor<T> descriptor;
    private volatile T instance;

    ServiceBinding(final ServiceDescriptor<T> descriptor) {
        this.descriptor = descriptor;
    }

    ServiceDescriptor<T> descriptor() {
        return descriptor;
    }

    @SuppressWarnings("unchecked")
    T instance() {
        return instance;
    }

    void setInstance(final T instance) {
        this.instance = instance;
    }

    boolean isInitialized() {
        return instance != null;
    }

    ServiceScope scope() {
        return descriptor.scope();
    }

    Class<T> type() {
        return descriptor.type();
    }
}
