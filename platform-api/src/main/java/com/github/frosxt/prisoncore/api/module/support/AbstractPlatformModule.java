package com.github.frosxt.prisoncore.api.module.support;

import com.github.frosxt.prisoncore.api.module.ModuleContext;
import com.github.frosxt.prisoncore.api.module.ModuleDescriptor;
import com.github.frosxt.prisoncore.api.module.PlatformModule;

import java.util.Objects;

/**
 * Base class for platform modules. Wires the descriptor lifecycle and delegates
 * prepare/enable/disable to {@link #onPrepare}/{@link #onEnable}/{@link #onDisable}.
 * Most external modules should extend this rather than implement {@link PlatformModule} directly.
 */
public abstract class AbstractPlatformModule implements PlatformModule {
    private ModuleDescriptor descriptor;

    @Override
    public final ModuleDescriptor descriptor() {
        if (descriptor == null) {
            throw new IllegalStateException("Module descriptor not available — prepare() has not been called yet");
        }
        return descriptor;
    }

    @Override
    public final void prepare(final ModuleContext context) {
        if (this.descriptor != null) {
            throw new IllegalStateException("Module has already been prepared");
        }
        this.descriptor = Objects.requireNonNull(context.descriptor(), "descriptor");
        onPrepare(context);
    }

    @Override
    public final void enable(final ModuleContext context) {
        onEnable(context);
    }

    @Override
    public final void disable(final ModuleContext context) {
        onDisable(context);
    }

    /** Construct services and resources; runs before any other module's {@code onEnable}. */
    protected abstract void onPrepare(ModuleContext context);

    /** Register commands, listeners, capabilities; runs once the platform is live. */
    protected abstract void onEnable(ModuleContext context);

    /** Release resources, cancel tasks, flush persistent state. Must be idempotent-safe. */
    protected abstract void onDisable(ModuleContext context);
}
