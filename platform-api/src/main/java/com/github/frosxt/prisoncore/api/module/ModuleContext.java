package com.github.frosxt.prisoncore.api.module;

import com.github.frosxt.prisoncore.api.capability.CapabilityRegistry;
import com.github.frosxt.prisoncore.api.service.ServiceContainer;

import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Runtime context handed to every module during prepare/enable/disable.
 * Gives a module access to its descriptor, the kernel service container for
 * dependency resolution, the capability registry, a module-scoped logger,
 * and the module's dedicated data folder on disk.
 */
public interface ModuleContext {

    /** @return the descriptor this module was loaded from. */
    ModuleDescriptor descriptor();

    /** @return the kernel service container; use it to resolve platform services. */
    ServiceContainer services();

    /** @return the capability registry; use it to publish or discover named capabilities. */
    CapabilityRegistry capabilities();

    /** @return a logger prefixed with this module's identity. */
    Logger logger();

    /** @return the module's dedicated data folder, created before {@code onPrepare} runs. */
    Path dataFolder();
}
