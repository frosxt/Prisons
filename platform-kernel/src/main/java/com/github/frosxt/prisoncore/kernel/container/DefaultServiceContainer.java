package com.github.frosxt.prisoncore.kernel.container;

import com.github.frosxt.prisoncore.api.module.ModuleHandle;
import com.github.frosxt.prisoncore.api.service.Provider;
import com.github.frosxt.prisoncore.api.service.ServiceContainer;
import com.github.frosxt.prisoncore.api.service.ServiceDescriptor;
import com.github.frosxt.prisoncore.api.service.ServiceException;
import com.github.frosxt.prisoncore.api.service.ServiceScope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class DefaultServiceContainer implements ServiceContainer {
    private final Map<Class<?>, ServiceBinding<?>> bindings = new ConcurrentHashMap<>();
    private final Map<String, ServiceBinding<?>> namedBindings = new ConcurrentHashMap<>();
    private final List<Class<?>> initializationOrder = new CopyOnWriteArrayList<>();

    @Override
    public <T> T resolve(final Class<T> type) {
        return resolveOptional(type)
                .orElseThrow(() -> new ServiceException("No service registered for: " + type.getName()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> resolveOptional(final Class<T> type) {
        final ServiceBinding<?> binding = bindings.get(type);
        if (binding == null) {
            return Optional.empty();
        }
        if (!binding.isInitialized()) {
            synchronized (binding) {
                if (!binding.isInitialized()) {
                    instantiate(binding);
                }
            }
        }
        return Optional.of((T) binding.instance());
    }

    @Override
    public <T> Provider<T> provider(final Class<T> type) {
        return () -> resolve(type);
    }

    @Override
    public <T> void register(final ServiceDescriptor<T> descriptor) {
        if (descriptor.scope() == ServiceScope.MODULE) {
            throw new ServiceException("Cannot register MODULE-scoped service '"
                    + descriptor.type().getName() + "' in the kernel container. Use a module scope.");
        }
        if (descriptor.scope() == ServiceScope.SESSION) {
            throw new ServiceException("Cannot register SESSION-scoped service '"
                    + descriptor.type().getName() + "' in the kernel container. Use a session scope.");
        }

        final ServiceBinding<T> binding = new ServiceBinding<>(descriptor);
        final ServiceBinding<?> existing = bindings.putIfAbsent(descriptor.type(), binding);
        if (existing != null) {
            throw new ServiceException("Duplicate service registration for: " + descriptor.type().getName()
                    + (descriptor.name() != null ? " (name: " + descriptor.name() + ")" : ""));
        }

        if (descriptor.name() != null) {
            namedBindings.put(descriptor.name(), binding);
        }
    }

    public <T> void registerOrReplace(final ServiceDescriptor<T> descriptor) {
        final ServiceBinding<?> old = bindings.put(descriptor.type(), new ServiceBinding<>(descriptor));
        if (old != null && old.isInitialized() && old.instance() instanceof final AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (final Exception ignored) {}
        }
        if (descriptor.name() != null) {
            namedBindings.put(descriptor.name(), bindings.get(descriptor.type()));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> resolveByName(final String name, final Class<T> type) {
        final ServiceBinding<?> binding = namedBindings.get(name);
        if (binding == null) {
            return Optional.empty();
        }
        if (!binding.isInitialized()) {
            synchronized (binding) {
                if (!binding.isInitialized()) {
                    instantiate(binding);
                }
            }
        }
        return Optional.of((T) binding.instance());
    }

    @Override
    public boolean has(final Class<?> type) {
        return bindings.containsKey(type);
    }

    @Override
    public Scope createModuleScope(final ModuleHandle moduleHandle) {
        return new ModuleScope(this, moduleHandle);
    }

    public void initialize() {
        final List<ServiceDescriptor<?>> order = DependencyGraphResolver.resolve(bindings.values());
        for (final ServiceDescriptor<?> desc : order) {
            final ServiceBinding<?> binding = bindings.get(desc.type());
            if (binding != null && !binding.isInitialized()) {
                instantiate(binding);
                initializationOrder.add(desc.type());
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void instantiate(final ServiceBinding<?> binding) {
        final ServiceDescriptor desc = binding.descriptor();
        final Object instance = desc.factory().apply(this);
        ((ServiceBinding) binding).setInstance(instance);
        if (desc.initCallback() != null) {
            desc.initCallback().accept(instance);
        }
    }

    public void shutdown() {
        final List<Class<?>> reversed = new ArrayList<>(initializationOrder);
        Collections.reverse(reversed);
        for (final Class<?> type : reversed) {
            final ServiceBinding<?> binding = bindings.get(type);
            if (binding != null && binding.isInitialized()) {
                invokeDestroyCallback(binding);
                if (binding.instance() instanceof final AutoCloseable closeable) {
                    try {
                        closeable.close();
                    } catch (final Exception ignored) {
                        // ignored
                    }
                }
            }
        }

        for (final ServiceBinding<?> binding : bindings.values()) {
            if (binding.isInitialized() && !initializationOrder.contains(binding.type())) {
                invokeDestroyCallback(binding);
                if (binding.instance() instanceof final AutoCloseable closeable) {
                    try {
                        closeable.close();
                    } catch (final Exception ignored) {
                        // ignored
                    }
                }
            }

        }
        bindings.clear();
        namedBindings.clear();
        initializationOrder.clear();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void invokeDestroyCallback(final ServiceBinding<?> binding) {
        final ServiceDescriptor desc = binding.descriptor();
        if (desc.destroyCallback() != null) {
            desc.destroyCallback().accept(binding.instance());
        }
    }
}
