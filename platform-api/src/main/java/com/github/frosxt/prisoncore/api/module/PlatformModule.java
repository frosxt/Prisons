package com.github.frosxt.prisoncore.api.module;

/**
 * The raw module contract. Most implementations should extend
 * {@link com.github.frosxt.prisoncore.api.module.support.AbstractPlatformModule}
 * instead of implementing this directly.
 *
 * <p>Lifecycle is strictly forward-only: {@code prepare} → {@code enable} → {@code disable}.
 * A module is constructed once per boot by the discovery mechanism.
 */
public interface PlatformModule {

    ModuleDescriptor descriptor();

    void prepare(ModuleContext context);

    void enable(ModuleContext context);

    void disable(ModuleContext context);
}
