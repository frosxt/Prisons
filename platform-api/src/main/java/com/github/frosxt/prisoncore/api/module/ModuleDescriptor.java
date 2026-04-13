package com.github.frosxt.prisoncore.api.module;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class ModuleDescriptor {
    private final String id;
    private final String name;
    private final String version;
    private final String apiVersion;
    private final String mainClass;
    private final ModuleLoadPhase loadPhase;
    private final List<String> requiredDependencies;
    private final List<String> optionalDependencies;
    private final Set<String> providesCapabilities;
    private final Set<String> requiresCapabilities;

    public ModuleDescriptor(final String id, final String name, final String version, final String apiVersion,
                            final String mainClass, final ModuleLoadPhase loadPhase,
                            final List<String> requiredDependencies, final List<String> optionalDependencies,
                            final Set<String> providesCapabilities, final Set<String> requiresCapabilities) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.version = Objects.requireNonNull(version, "version");
        this.apiVersion = Objects.requireNonNull(apiVersion, "apiVersion");
        this.mainClass = Objects.requireNonNull(mainClass, "mainClass");
        this.loadPhase = loadPhase != null ? loadPhase : ModuleLoadPhase.POST_INFRASTRUCTURE;
        this.requiredDependencies = Collections.unmodifiableList(requiredDependencies);
        this.optionalDependencies = Collections.unmodifiableList(optionalDependencies);
        this.providesCapabilities = Collections.unmodifiableSet(providesCapabilities);
        this.requiresCapabilities = Collections.unmodifiableSet(requiresCapabilities);
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String version() {
        return version;
    }

    public String apiVersion() {
        return apiVersion;
    }

    public String mainClass() {
        return mainClass;
    }

    public ModuleLoadPhase loadPhase() {
        return loadPhase;
    }

    public List<String> requiredDependencies() {
        return requiredDependencies;
    }

    public List<String> optionalDependencies() {
        return optionalDependencies;
    }

    public Set<String> providesCapabilities() {
        return providesCapabilities;
    }

    public Set<String> requiresCapabilities() {
        return requiresCapabilities;
    }

    @Override
    public String toString() {
        return name + " v" + version + " [" + id + "]";
    }
}
