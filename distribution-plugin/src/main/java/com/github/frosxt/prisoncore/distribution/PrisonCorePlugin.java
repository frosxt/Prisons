package com.github.frosxt.prisoncore.distribution;

import com.github.frosxt.prisoncore.runtime.bukkit.BukkitKernelBootstrap;
import org.bukkit.plugin.java.JavaPlugin;

public final class PrisonCorePlugin extends JavaPlugin {
    private final BukkitKernelBootstrap bootstrap = new BukkitKernelBootstrap();

    @Override
    public void onLoad() {
        bootstrap.onLoad(this);
    }

    @Override
    public void onEnable() {
        bootstrap.onEnable(this);
    }

    @Override
    public void onDisable() {
        bootstrap.onDisable(this);
    }
}
