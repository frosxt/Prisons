package com.github.frosxt.prisoncore.runtime.bukkit.adapter;

import java.util.logging.Logger;

public final class BukkitLoggerAdapter {
    private final Logger logger;

    public BukkitLoggerAdapter(final Logger logger) {
        this.logger = logger;
    }

    public Logger logger() {
        return logger;
    }
}
