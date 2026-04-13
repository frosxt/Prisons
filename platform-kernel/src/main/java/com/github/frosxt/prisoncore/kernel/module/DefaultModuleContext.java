package com.github.frosxt.prisoncore.kernel.module;

import com.github.frosxt.prisoncore.api.capability.CapabilityRegistry;
import com.github.frosxt.prisoncore.api.module.ModuleContext;
import com.github.frosxt.prisoncore.api.module.ModuleDescriptor;
import com.github.frosxt.prisoncore.api.service.ServiceContainer;

import java.nio.file.Path;
import java.util.logging.Logger;

public final class DefaultModuleContext implements ModuleContext {
    private final ModuleDescriptor descriptor;
    private final ServiceContainer.Scope services;
    private final CapabilityRegistry capabilities;
    private final Logger logger;
    private final Path dataFolder;

    public DefaultModuleContext(final ModuleDescriptor descriptor, final ServiceContainer.Scope services,
                                final CapabilityRegistry capabilities, final Logger logger,
                                final Path dataFolder) {
        this.descriptor = descriptor;
        this.services = services;
        this.capabilities = capabilities;
        this.logger = logger;
        this.dataFolder = dataFolder;
    }

    @Override
    public ModuleDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public ServiceContainer services() {
        return services;
    }

    @Override
    public CapabilityRegistry capabilities() {
        return capabilities;
    }

    @Override
    public Logger logger() {
        return logger;
    }

    @Override
    public Path dataFolder() {
        return dataFolder;
    }
}
