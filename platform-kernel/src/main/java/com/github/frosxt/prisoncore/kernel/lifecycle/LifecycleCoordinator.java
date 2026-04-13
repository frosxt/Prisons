package com.github.frosxt.prisoncore.kernel.lifecycle;

import com.github.frosxt.prisoncore.api.lifecycle.LifecycleState;
import com.github.frosxt.prisoncore.kernel.Kernel;

import java.util.logging.Logger;

public final class LifecycleCoordinator {
    private final Kernel kernel;
    private final Logger logger;

    public LifecycleCoordinator(final Kernel kernel, final Logger logger) {
        this.kernel = kernel;
        this.logger = logger;
    }

    public void bootstrap() {
        logger.info("[PrisonCore] Beginning bootstrap...");
        kernel.stateMachine().transition(LifecycleState.BOOTSTRAPPING);
    }

    public void markInfrastructureReady() {
        kernel.stateMachine().transition(LifecycleState.INFRASTRUCTURE_READY);
        logger.info("[PrisonCore] Infrastructure services ready.");
    }

    public void markModulesDiscovered() {
        kernel.stateMachine().transition(LifecycleState.MODULES_DISCOVERED);
    }

    public void markModulesResolved() {
        kernel.stateMachine().transition(LifecycleState.MODULES_RESOLVED);
    }

    public void markModulesPrepared() {
        kernel.stateMachine().transition(LifecycleState.MODULES_PREPARED);
    }

    public void markModulesEnabled() {
        kernel.stateMachine().transition(LifecycleState.MODULES_ENABLED);
    }

    public void activate() {
        kernel.stateMachine().transition(LifecycleState.ACTIVE);
        logger.info("[PrisonCore] Platform is now ACTIVE.");
    }

    public void quiesce() {
        kernel.stateMachine().transition(LifecycleState.QUIESCING);
        logger.info("[PrisonCore] Platform is quiescing...");
    }

    public void disable() {
        kernel.stateMachine().transition(LifecycleState.DISABLED);
        logger.info("[PrisonCore] Platform is now DISABLED.");
    }
}
