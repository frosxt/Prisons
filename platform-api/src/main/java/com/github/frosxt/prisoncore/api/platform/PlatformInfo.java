package com.github.frosxt.prisoncore.api.platform;

import com.github.frosxt.prisoncore.api.lifecycle.LifecycleState;

import java.nio.file.Path;

public interface PlatformInfo {
    String version();

    LifecycleState state();

    Path dataFolder();

    String serverVersion();
}
