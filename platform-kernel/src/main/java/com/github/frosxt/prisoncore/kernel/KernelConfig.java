package com.github.frosxt.prisoncore.kernel;

import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Logger;

public final class KernelConfig {
    private final Path dataFolder;
    private final Path modulesDir;
    private final Logger logger;

    public KernelConfig(final Path dataFolder, final Path modulesDir, final Logger logger) {
        this.dataFolder = Objects.requireNonNull(dataFolder);
        this.modulesDir = Objects.requireNonNull(modulesDir);
        this.logger = Objects.requireNonNull(logger);
    }

    public Path dataFolder() {
        return dataFolder;
    }

    public Path modulesDir() {
        return modulesDir;
    }

    public Logger logger() {
        return logger;
    }
}
