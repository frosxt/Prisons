package com.github.frosxt.prisoncore.kernel.module;

import com.github.frosxt.prisoncore.api.capability.CapabilityProvider;
import com.github.frosxt.prisoncore.api.capability.CapabilityRegistry;
import com.github.frosxt.prisoncore.api.module.ModuleBootstrap;
import com.github.frosxt.prisoncore.api.module.ModuleDescriptor;
import com.github.frosxt.prisoncore.api.module.ModuleState;
import com.github.frosxt.prisoncore.api.module.PlatformModule;
import com.github.frosxt.prisoncore.api.service.ServiceContainer;
import com.github.frosxt.prisoncore.kernel.migrations.MigrationCoordinator;
import com.github.frosxt.prisoncore.kernel.module.classloader.IsolatedModuleClassLoader;
import com.github.frosxt.prisoncore.kernel.module.graph.ModuleGraphResolver;
import com.github.frosxt.prisoncore.kernel.module.startup.StartupReport;
import com.github.frosxt.prisoncore.spi.migration.MigrationProvider;
import com.github.frosxt.prisoncore.spi.module.ModuleCandidate;
import com.github.frosxt.prisoncore.spi.module.ModuleDiscoverer;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class DefaultModuleManager {
    private final Logger logger;
    private final Path modulesDir;
    private final Path moduleDataDir;
    private final ServiceContainer container;
    private final CapabilityRegistry capabilityRegistry;
    private final ModuleDiscoverer discoverer;
    private final StartupReport report;

    private final Map<String, DefaultModuleHandle> handles = new ConcurrentHashMap<>();
    private final Map<String, PlatformModule> modules = new ConcurrentHashMap<>();
    private final Map<String, ModuleBootstrap> bootstraps = new ConcurrentHashMap<>();
    private final Map<String, ServiceContainer.Scope> scopes = new ConcurrentHashMap<>();
    private final List<String> allEnabledOrder = new ArrayList<>();
    private List<ModuleCandidate> resolvedOrder = new ArrayList<>();
    private boolean reportStarted;

    public DefaultModuleManager(final Logger logger, final Path modulesDir, final Path moduleDataDir,
                                final ServiceContainer container, final CapabilityRegistry capabilityRegistry,
                                final ModuleDiscoverer discoverer) {
        this.logger = logger;
        this.modulesDir = modulesDir;
        this.moduleDataDir = moduleDataDir;
        this.container = container;
        this.capabilityRegistry = capabilityRegistry;
        this.discoverer = discoverer;
        this.report = new StartupReport();
    }

    public List<ModuleCandidate> discover() {
        final List<ModuleCandidate> candidates = discoverer.discover(modulesDir);
        final Map<String, ModuleCandidate> uniqueById = new LinkedHashMap<>();
        for (final ModuleCandidate candidate : candidates) {
            final String id = candidate.descriptor().id();
            if (uniqueById.containsKey(id)) {
                logger.warning("[PrisonCore] Duplicate module ID '" + id + "' found in "
                        + candidate.jarPath().getFileName() + " — skipping (keeping "
                        + uniqueById.get(id).jarPath().getFileName() + ")");
            } else {
                uniqueById.put(id, candidate);
            }
        }
        return new ArrayList<>(uniqueById.values());
    }

    public List<ModuleCandidate> resolve(final List<ModuleCandidate> candidates) {
        this.resolvedOrder = ModuleGraphResolver.resolve(candidates);

        final Set<String> resolvedIds = resolvedOrder.stream()
                .map(c -> c.descriptor().id())
                .collect(Collectors.toSet());
        for (final ModuleCandidate candidate : candidates) {
            final String id = candidate.descriptor().id();
            if (!resolvedIds.contains(id)) {
                logger.warning("[PrisonCore] Module '" + id + "' was rejected during resolution "
                        + "(missing dependency or capability)");
                report.addEntry(id, ModuleState.FAILED, 0,
                        "Rejected: missing required dependency or capability");
            }
        }

        return resolvedOrder;
    }

    public void prepareAll() {
        if (!reportStarted) {
            report.markStart();
            reportStarted = true;
        }
        for (final ModuleCandidate candidate : resolvedOrder) {
            final DefaultModuleHandle existing = handles.get(candidate.descriptor().id());
            if (existing != null && existing.state() != ModuleState.DISCOVERED) {
                continue;
            }
            prepareModule(candidate);
        }
    }

    public void enableAll() {
        for (final ModuleCandidate candidate : resolvedOrder) {
            final DefaultModuleHandle existing = handles.get(candidate.descriptor().id());
            if (existing != null && existing.state() == ModuleState.ENABLED) {
                continue;
            }
            enableModule(candidate.descriptor().id());
        }
    }

    private void prepareModule(final ModuleCandidate candidate) {
        final ModuleDescriptor desc = candidate.descriptor();
        final long start = System.currentTimeMillis();

        try {
            final URL jarUrl = candidate.jarPath().toUri().toURL();
            final ClassLoader classLoader = new IsolatedModuleClassLoader(
                    new URL[]{jarUrl}, getClass().getClassLoader());

            final DefaultModuleHandle handle = new DefaultModuleHandle(desc, classLoader);
            handles.put(desc.id(), handle);

            final Class<?> bootstrapClass = classLoader.loadClass(desc.mainClass());
            final ModuleBootstrap bootstrap = (ModuleBootstrap) bootstrapClass.getConstructor().newInstance();

            final ServiceContainer.Scope scope = container.createModuleScope(handle);
            scopes.put(desc.id(), scope);

            final Path dataFolder = moduleDataDir.resolve(desc.id());
            if (!Files.exists(dataFolder)) {
                Files.createDirectories(dataFolder);
            }

            final Logger moduleLogger = Logger.getLogger("PrisonCore|" + desc.name());
            final DefaultModuleContext context = new DefaultModuleContext(desc, scope, capabilityRegistry, moduleLogger, dataFolder);

            final PlatformModule module = bootstrap.create(context);
            modules.put(desc.id(), module);
            bootstraps.put(desc.id(), bootstrap);

            module.prepare(context);
            handle.setState(ModuleState.PREPARED);

            final long duration = System.currentTimeMillis() - start;
            logger.info("[PrisonCore] Prepared module: " + desc + " (" + duration + "ms)");

        } catch (final Exception e) {
            final long duration = System.currentTimeMillis() - start;
            final DefaultModuleHandle handle = handles.remove(desc.id());
            if (handle != null) {
                handle.setState(ModuleState.FAILED);
            }

            final ServiceContainer.Scope scope = scopes.remove(desc.id());
            if (scope != null) {
                try {
                    scope.close();
                } catch (final Exception ignored) {}
            }
            if (handle != null && handle.classLoader() instanceof final AutoCloseable closeable) {
                try {
                    closeable.close();
                } catch (final Exception ignored) {}
            }

            modules.remove(desc.id());
            report.addEntry(desc.id(), ModuleState.FAILED, duration, e.getMessage());
            logger.log(Level.SEVERE, "[PrisonCore] Failed to prepare module: " + desc.id(), e);
        }
    }

    private void enableModule(final String moduleId) {
        final DefaultModuleHandle handle = handles.get(moduleId);
        if (handle == null || handle.state() == ModuleState.FAILED) {
            return;
        }

        final ModuleDescriptor desc = handle.descriptor();
        final long start = System.currentTimeMillis();

        try {
            final PlatformModule module = modules.get(moduleId);
            final ServiceContainer.Scope scope = scopes.get(moduleId);
            final Path dataFolder = moduleDataDir.resolve(moduleId);
            final Logger moduleLogger = Logger.getLogger("PrisonCore|" + desc.name());
            final DefaultModuleContext context = new DefaultModuleContext(desc, scope, capabilityRegistry, moduleLogger, dataFolder);

            final ModuleBootstrap bootstrap = bootstraps.get(moduleId);
            if (bootstrap instanceof final MigrationProvider migrationProvider) {
                container.resolveOptional(MigrationCoordinator.class)
                        .ifPresent(coordinator -> coordinator.addProvider(migrationProvider));
            }

            container.resolveOptional(MigrationCoordinator.class)
                    .ifPresent(coordinator -> coordinator.runMigrations(moduleId,
                            new DefaultMigrationContext(moduleLogger, container)));

            module.enable(context);

            if (module instanceof final CapabilityProvider capabilityProvider) {
                capabilityProvider.registerCapabilities(capabilityRegistry);
            }

            handle.setState(ModuleState.ENABLED);
            allEnabledOrder.add(moduleId);

            for (final String capability : desc.providesCapabilities()) {
                capabilityRegistry.registerMarker(capability);
            }

            final long duration = System.currentTimeMillis() - start;
            report.addEntry(moduleId, ModuleState.ENABLED, duration, null);

        } catch (final Exception e) {
            final long duration = System.currentTimeMillis() - start;
            handle.setState(ModuleState.FAILED);
            report.addEntry(moduleId, ModuleState.FAILED, duration, e.getMessage());
            logger.log(Level.SEVERE, "[PrisonCore] Failed to enable module: " + moduleId, e);

            final ServiceContainer.Scope scope = scopes.remove(moduleId);
            if (scope != null) {
                try { scope.close(); } catch (final Exception ignored) {}
            }
            if (handle.classLoader() instanceof final AutoCloseable closeable) {
                try { closeable.close(); } catch (final Exception ignored) {}
            }
            modules.remove(moduleId);
        }
    }

    public void disableAll() {
        final List<String> reverseOrder = new ArrayList<>(allEnabledOrder);
        Collections.reverse(reverseOrder);

        for (final String moduleId : reverseOrder) {
            disableModule(moduleId);
        }
    }

    private void disableModule(final String moduleId) {
        final DefaultModuleHandle handle = handles.get(moduleId);
        if (handle == null || handle.state() != ModuleState.ENABLED) {
            return;
        }

        handle.setState(ModuleState.DISABLING);
        final ModuleDescriptor desc = handle.descriptor();

        try {
            final PlatformModule module = modules.get(moduleId);
            final ServiceContainer.Scope scope = scopes.get(moduleId);
            final Path dataFolder = moduleDataDir.resolve(moduleId);
            final Logger moduleLogger = Logger.getLogger("PrisonCore|" + desc.name());
            final DefaultModuleContext context = new DefaultModuleContext(desc, scope, capabilityRegistry, moduleLogger, dataFolder);

            module.disable(context);
            handle.setState(ModuleState.DISABLED);
            logger.info("[PrisonCore] Disabled module: " + desc);

        } catch (final Exception e) {
            handle.setState(ModuleState.FAILED);
            logger.log(Level.WARNING, "[PrisonCore] Error disabling module: " + moduleId, e);
        } finally {
            final ServiceContainer.Scope scope = scopes.remove(moduleId);
            if (scope != null) {
                try { scope.close(); } catch (final Exception ignored) {}
            }
            if (handle.classLoader() instanceof final AutoCloseable closeable) {
                try { closeable.close(); } catch (final Exception ignored) {}
            }
            modules.remove(moduleId);
            handles.remove(moduleId);
        }
    }

    public StartupReport report() {
        return report;
    }

    public Collection<DefaultModuleHandle> allHandles() {
        return handles.values();
    }

    public List<ModuleCandidate> resolvedOrder() {
        return Collections.unmodifiableList(resolvedOrder);
    }

    public DefaultModuleHandle handle(final String moduleId) {
        return handles.get(moduleId);
    }

    public boolean enableById(final String moduleId) {
        final DefaultModuleHandle existing = handles.get(moduleId);
        if (existing != null && existing.state() == ModuleState.ENABLED) {
            return true;
        }

        final ModuleCandidate candidate = findResolvedCandidate(moduleId);
        if (candidate == null) {
            logger.warning("[PrisonCore] Cannot enable unknown module: " + moduleId);
            return false;
        }

        final List<String> unmetDependencies = findUnmetDependencies(candidate.descriptor());
        if (!unmetDependencies.isEmpty()) {
            logger.warning("[PrisonCore] Cannot enable '" + moduleId
                    + "' — missing enabled dependencies: " + unmetDependencies);
            return false;
        }

        if (existing == null || existing.state() == ModuleState.DISABLED || existing.state() == ModuleState.FAILED) {
            prepareModule(candidate);
        }

        enableModule(moduleId);
        final DefaultModuleHandle handle = handles.get(moduleId);
        return handle != null && handle.state() == ModuleState.ENABLED;
    }

    public boolean disableById(final String moduleId) {
        final DefaultModuleHandle handle = handles.get(moduleId);
        if (handle == null || handle.state() != ModuleState.ENABLED) {
            return false;
        }

        final List<String> dependents = findEnabledDependents(moduleId);
        if (!dependents.isEmpty()) {
            logger.warning("[PrisonCore] Cannot disable '" + moduleId
                    + "' — enabled modules depend on it: " + dependents);
            return false;
        }

        disableModule(moduleId);
        allEnabledOrder.remove(moduleId);
        return true;
    }

    public boolean reloadById(final String moduleId) {
        if (handles.get(moduleId) == null) {
            return false;
        }
        if (!disableById(moduleId)) {
            return false;
        }
        return enableById(moduleId);
    }

    private ModuleCandidate findResolvedCandidate(final String moduleId) {
        for (final ModuleCandidate candidate : resolvedOrder) {
            if (candidate.descriptor().id().equals(moduleId)) {
                return candidate;
            }
        }
        return null;
    }

    private List<String> findUnmetDependencies(final ModuleDescriptor descriptor) {
        final List<String> missing = new ArrayList<>();
        for (final String dep : descriptor.requiredDependencies()) {
            final DefaultModuleHandle depHandle = handles.get(dep);
            if (depHandle == null || depHandle.state() != ModuleState.ENABLED) {
                missing.add(dep);
            }
        }
        return missing;
    }

    private List<String> findEnabledDependents(final String moduleId) {
        final List<String> dependents = new ArrayList<>();
        for (final DefaultModuleHandle handle : handles.values()) {
            if (handle.state() != ModuleState.ENABLED) {
                continue;
            }
            if (handle.descriptor().requiredDependencies().contains(moduleId)) {
                dependents.add(handle.descriptor().id());
            }
        }
        return dependents;
    }
}
