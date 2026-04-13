package com.github.frosxt.prisoncore.runtime.bukkit.adapter;

import com.github.frosxt.prisoncore.api.lifecycle.LifecycleState;
import com.github.frosxt.prisoncore.api.platform.PlatformInfo;
import com.github.frosxt.prisoncore.kernel.Kernel;
import org.bukkit.Bukkit;

import java.nio.file.Path;

public final class BukkitPlatformInfo implements PlatformInfo {
    private final Kernel kernel;
    private final String version;

    public BukkitPlatformInfo(final Kernel kernel, final String version) {
        this.kernel = kernel;
        this.version = version;
    }

    @Override
    public String version() {
        return version;
    }

    @Override
    public LifecycleState state() {
        return kernel.state();
    }

    @Override
    public Path dataFolder() {
        return kernel.config().dataFolder();
    }

    @Override
    public String serverVersion() {
        return Bukkit.getVersion();
    }
}
