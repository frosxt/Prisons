package com.github.frosxt.prisoncore.kernel.container;

import com.github.frosxt.prisoncore.api.module.ModuleHandle;
import com.github.frosxt.prisoncore.api.service.Provider;
import com.github.frosxt.prisoncore.api.service.ServiceContainer;
import com.github.frosxt.prisoncore.api.service.ServiceDescriptor;
import com.github.frosxt.prisoncore.api.service.ServiceException;
import com.github.frosxt.prisoncore.api.service.ServiceScope;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ModuleScope implements ServiceContainer.Scope {
    private final DefaultServiceContainer parent;
    private final ModuleHandle owner;
    private final Map<Class<?>, ServiceBinding<?>> localBindings = new ConcurrentHashMap<>();

    ModuleScope(final DefaultServiceContainer parent, final ModuleHandle owner) {
        this.parent = parent;
        this.owner = owner;
    }

    @Override
    public ModuleHandle owner() {
        return owner;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> T resolve(final Class<T> type) {
        final ServiceBinding<?> local = localBindings.get(type);
        if (local != null) {
            if (!local.isInitialized()) {
                synchronized (local) {
                    if (!local.isInitialized()) {
                        final Object instance = local.descriptor().factory().apply(this);
                        ((ServiceBinding<Object>) local).setInstance(instance);
                        final ServiceDescriptor desc = local.descriptor();
                        if (desc.initCallback() != null) {
                            desc.initCallback().accept(instance);
                        }
                    }
                }
            }
            return (T) local.instance();
        }
        return parent.resolve(type);
    }

    @Override
    public <T> Optional<T> resolveOptional(final Class<T> type) {
        final ServiceBinding<?> local = localBindings.get(type);
        if (local != null) {
            return Optional.of(resolve(type));
        }
        return parent.resolveOptional(type);
    }

    @Override
    public <T> Optional<T> resolveByName(final String name, final Class<T> type) {
        return parent.resolveByName(name, type);
    }

    @Override
    public boolean has(final Class<?> type) {
        return localBindings.containsKey(type) || parent.has(type);
    }

    @Override
    public <T> Provider<T> provider(final Class<T> type) {
        return () -> resolve(type);
    }

    @Override
    public <T> void register(final ServiceDescriptor<T> descriptor) {
        if (descriptor.scope() != ServiceScope.KERNEL) {
            final ServiceBinding<?> existing = localBindings.putIfAbsent(descriptor.type(), new ServiceBinding<>(descriptor));
            if (existing != null) {
                throw new ServiceException("Duplicate service registration in module scope '"
                        + owner.descriptor().id() + "' for: " + descriptor.type().getName());
            }
        } else {
            throw new ServiceException("Cannot register KERNEL-scoped service '"
                    + descriptor.type().getName() + "' in module scope '" + owner.descriptor().id() + "'");
        }
    }

    @Override
    public Scope createModuleScope(final ModuleHandle moduleHandle) {
        throw new UnsupportedOperationException("Cannot create nested module scopes");
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void close() {
        for (final ServiceBinding<?> binding : localBindings.values()) {
            if (binding.isInitialized()) {
                final ServiceDescriptor desc = binding.descriptor();
                if (desc.destroyCallback() != null) {
                    desc.destroyCallback().accept(binding.instance());
                }
                if (binding.instance() instanceof final AutoCloseable closeable) {
                    try {
                        closeable.close();
                    } catch (final Exception e) {
                        // Swallow
                    }
                }
            }
        }
        localBindings.clear();
    }
}
