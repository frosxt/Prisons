package com.github.frosxt.prisoncore.spi.module;

import com.github.frosxt.prisoncore.api.module.ModuleDescriptor;

import java.nio.file.Path;
import java.util.Objects;

public final class ModuleCandidate {
    private final Path jarPath;
    private final ModuleDescriptor descriptor;

    public ModuleCandidate(final Path jarPath, final ModuleDescriptor descriptor) {
        this.jarPath = Objects.requireNonNull(jarPath);
        this.descriptor = Objects.requireNonNull(descriptor);
    }

    public Path jarPath() {
        return jarPath;
    }

    public ModuleDescriptor descriptor() {
        return descriptor;
    }
}
