package com.github.frosxt.prisoncore.kernel;

import com.github.frosxt.prisoncore.api.capability.CapabilityRegistry;
import com.github.frosxt.prisoncore.api.event.DomainEventBus;
import com.github.frosxt.prisoncore.api.lifecycle.LifecycleState;
import com.github.frosxt.prisoncore.api.module.ModuleLoadPhase;
import com.github.frosxt.prisoncore.api.service.ServiceDescriptor;
import com.github.frosxt.prisoncore.commons.api.cache.CacheFactory;
import com.github.frosxt.prisoncore.commons.core.cache.DefaultCacheFactory;
import com.github.frosxt.prisoncore.commons.core.cooldown.CooldownTracker;
import com.github.frosxt.prisoncore.kernel.capability.DefaultCapabilityRegistry;
import com.github.frosxt.prisoncore.kernel.config.CoreConfig;
import com.github.frosxt.prisoncore.kernel.container.DefaultServiceContainer;
import com.github.frosxt.prisoncore.kernel.container.session.SessionScopeManager;
import com.github.frosxt.prisoncore.kernel.event.DefaultDomainEventBus;
import com.github.frosxt.prisoncore.kernel.lifecycle.LifecycleCoordinator;
import com.github.frosxt.prisoncore.kernel.lifecycle.LifecycleStateMachine;
import com.github.frosxt.prisoncore.kernel.migrations.MigrationCoordinator;
import com.github.frosxt.prisoncore.kernel.module.DefaultModuleManager;
import com.github.frosxt.prisoncore.kernel.module.discovery.AnnotationModuleDiscoverer;
import com.github.frosxt.prisoncore.kernel.observability.ObservabilityService;
import com.github.frosxt.prisoncore.kernel.shutdown.ShutdownBarrier;
import com.github.frosxt.prisoncore.kernel.storage.StorageRegistry;
import com.github.frosxt.prisoncore.spi.module.ModuleCandidate;

import java.util.ArrayList;
import java.util.List;

public final class Kernel {
    private final KernelConfig config;
    private final LifecycleStateMachine stateMachine;
    private final LifecycleCoordinator coordinator;
    private final DefaultServiceContainer container;
    private final DefaultCapabilityRegistry capabilityRegistry;
    private final DefaultDomainEventBus eventBus;
    private final ShutdownBarrier shutdownBarrier;
    private final StorageRegistry storageRegistry;
    private final ObservabilityService observability;
    private final SessionScopeManager sessionScopeManager;
    private CoreConfig coreConfig;
    private DefaultModuleManager moduleManager;

    public Kernel(final KernelConfig config) {
        this.config = config;
        this.stateMachine = new LifecycleStateMachine();
        this.coordinator = new LifecycleCoordinator(this, config.logger());
        this.container = new DefaultServiceContainer();
        this.capabilityRegistry = new DefaultCapabilityRegistry();
        this.eventBus = new DefaultDomainEventBus(config.logger());
        this.shutdownBarrier = new ShutdownBarrier(config.logger());
        this.storageRegistry = new StorageRegistry(config.logger());
        this.observability = new ObservabilityService();
        this.sessionScopeManager = new SessionScopeManager(container);
    }

    /**
     * Bootstrap follows the lifecycle enum order strictly:
     * CREATED -> BOOTSTRAPPING -> INFRASTRUCTURE_READY -> MODULES_DISCOVERED
     * -> MODULES_RESOLVED -> MODULES_PREPARED
     *
     * PRE_INFRASTRUCTURE modules are loaded during the BOOTSTRAPPING phase
     * (before INFRASTRUCTURE_READY) so they can contribute storage backends
     * and other infrastructure services.
     */
    /**
     * Set the core config before calling bootstrap().
     * The runtime layer is responsible for loading and parsing core.yml
     * using the platform YAML parser (e.g. Bukkit YamlConfiguration).
     */
    public void setCoreConfig(final CoreConfig coreConfig) {
        this.coreConfig = coreConfig;
    }

    public void bootstrap() {
        final long startTime = System.currentTimeMillis();
        coordinator.bootstrap(); // CREATED -> BOOTSTRAPPING

        if (coreConfig == null) {
            coreConfig = CoreConfig.defaults();
        }
        if (coreConfig.debugMode()) {
            config.logger().info("[PrisonCore] Debug mode enabled.");
        }

        container.register(ServiceDescriptor.builder(CoreConfig.class)
                .instance(coreConfig).build());
        container.register(ServiceDescriptor.builder(CapabilityRegistry.class)
                .instance(capabilityRegistry).build());
        container.register(ServiceDescriptor.builder(DomainEventBus.class)
                .instance(eventBus).build());
        container.register(ServiceDescriptor.builder(ShutdownBarrier.class)
                .instance(shutdownBarrier).build());
        container.register(ServiceDescriptor.builder(StorageRegistry.class)
                .instance(storageRegistry).build());
        container.register(ServiceDescriptor.builder(ObservabilityService.class)
                .instance(observability).build());
        container.register(ServiceDescriptor.builder(SessionScopeManager.class)
                .instance(sessionScopeManager).build());
        container.register(ServiceDescriptor.builder(MigrationCoordinator.class)
                .factory(c -> new MigrationCoordinator(config.logger(), config.dataFolder())).build());

        container.register(ServiceDescriptor.builder(CacheFactory.class)
                .factory(c -> new DefaultCacheFactory()).build());
        container.register(ServiceDescriptor.builder(CooldownTracker.class)
                .factory(c -> new CooldownTracker()).build());

        final AnnotationModuleDiscoverer discoverer = new AnnotationModuleDiscoverer(config.logger());
        moduleManager = new DefaultModuleManager(
                config.logger(), config.modulesDir(), config.modulesDir(), container, capabilityRegistry, discoverer);
        final List<ModuleCandidate> candidates = moduleManager.discover();

        final List<ModuleCandidate> preInfra = new ArrayList<>();
        final List<ModuleCandidate> postInfra = new ArrayList<>();
        for (final ModuleCandidate candidate : candidates) {
            if (candidate.descriptor().loadPhase() == ModuleLoadPhase.PRE_INFRASTRUCTURE) {
                preInfra.add(candidate);
            } else {
                postInfra.add(candidate);
            }
        }

        // PRE_INFRASTRUCTURE modules load now, during BOOTSTRAPPING,
        if (!preInfra.isEmpty()) {
            config.logger().info("[PrisonCore] Processing " + preInfra.size() + " PRE_INFRASTRUCTURE module(s)...");
            moduleManager.resolve(preInfra);
            moduleManager.prepareAll();
            moduleManager.enableAll();
        }

        container.initialize();
        coordinator.markInfrastructureReady(); // BOOTSTRAPPING -> INFRASTRUCTURE_READY

        observability.recordTiming("kernel.infrastructure", System.currentTimeMillis() - startTime);

        // Now handle POST_INFRASTRUCTURE and LATE modules
        coordinator.markModulesDiscovered(); // INFRASTRUCTURE_READY -> MODULES_DISCOVERED

        if (!postInfra.isEmpty()) {
            moduleManager.resolve(postInfra);
        }
        coordinator.markModulesResolved(); // MODULES_DISCOVERED -> MODULES_RESOLVED

        moduleManager.prepareAll();
        coordinator.markModulesPrepared(); // MODULES_RESOLVED -> MODULES_PREPARED
    }

    public void activate() {
        moduleManager.enableAll();
        coordinator.markModulesEnabled(); // MODULES_PREPARED -> MODULES_ENABLED
        coordinator.activate();           // MODULES_ENABLED -> ACTIVE

        moduleManager.report().print(config.logger());
    }

    public void shutdown() {
        coordinator.quiesce(); // ACTIVE -> QUIESCING
        shutdownBarrier.engage(10000);

        sessionScopeManager.destroyAll();

        if (moduleManager != null) {
            moduleManager.disableAll();
        }

        storageRegistry.shutdownAll();
        eventBus.shutdown();
        container.shutdown();
        coordinator.disable(); // QUIESCING -> DISABLED
    }

    public <T> void registerService(final Class<T> type, final T instance) {
        container.registerOrReplace(ServiceDescriptor.builder(type).instance(instance).build());
    }

    public LifecycleStateMachine stateMachine() {
        return stateMachine;
    }

    public LifecycleState state() {
        return stateMachine.current();
    }

    public DefaultServiceContainer container() {
        return container;
    }

    public CapabilityRegistry capabilityRegistry() {
        return capabilityRegistry;
    }

    public DomainEventBus eventBus() {
        return eventBus;
    }

    public DefaultModuleManager moduleManager() {
        return moduleManager;
    }

    public KernelConfig config() {
        return config;
    }

    public CoreConfig coreConfig() {
        return coreConfig;
    }

    public StorageRegistry storageRegistry() {
        return storageRegistry;
    }

    public ObservabilityService observability() {
        return observability;
    }

    public SessionScopeManager sessionScopeManager() {
        return sessionScopeManager;
    }
}
