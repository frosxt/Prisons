package com.github.frosxt.prisoncore.spi.module;

import java.nio.file.Path;
import java.util.List;

public interface ModuleDiscoverer {
    List<ModuleCandidate> discover(Path modulesDir);
}
