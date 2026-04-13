package com.github.frosxt.prisoncore.kernel.module;

import com.github.frosxt.prisoncore.api.module.ModuleDescriptor;
import com.github.frosxt.prisoncore.api.module.ModuleHandle;
import com.github.frosxt.prisoncore.api.module.ModuleState;

public final class DefaultModuleHandle implements ModuleHandle {
    private final ModuleDescriptor descriptor;
    private volatile ModuleState state;
    private final ClassLoader classLoader;

    public DefaultModuleHandle(final ModuleDescriptor descriptor, final ClassLoader classLoader) {
        this.descriptor = descriptor;
        this.state = ModuleState.DISCOVERED;
        this.classLoader = classLoader;
    }

    @Override
    public ModuleDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public ModuleState state() {
        return state;
    }

    @Override
    public ClassLoader classLoader() {
        return classLoader;
    }

    public void setState(final ModuleState state) {
        this.state = state;
    }
}
