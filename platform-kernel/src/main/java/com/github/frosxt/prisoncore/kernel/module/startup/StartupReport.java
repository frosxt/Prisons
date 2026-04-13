package com.github.frosxt.prisoncore.kernel.module.startup;

import com.github.frosxt.prisoncore.api.module.ModuleState;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class StartupReport {
    private final List<ModuleEntry> entries = new ArrayList<>();
    private long startTime;

    public void markStart() { this.startTime = System.currentTimeMillis(); }

    public void addEntry(final String moduleId, final ModuleState state, final long durationMs, final String error) {
        entries.add(new ModuleEntry(moduleId, state, durationMs, error));
    }

    public void print(final Logger logger) {
        final long totalTime = System.currentTimeMillis() - startTime;
        logger.info("╔══════════════════════════════════════════════════════╗");
        logger.info("║             PrisonCore Startup Report                ║");
        logger.info("╠══════════════════════════════════════════════════════╣");

        int enabled = 0;
        int failed = 0;

        for (final ModuleEntry entry : entries) {
            final String status = entry.state == ModuleState.ENABLED ? "✓" : "✗";
            final String line = String.format("║  %s %-30s %6dms  %-8s ║",
                    status, entry.moduleId, entry.durationMs, entry.state);
            logger.info(line);
            if (entry.state == ModuleState.ENABLED) {
                enabled++;
            } else {
                failed++;
            }
            if (entry.error != null) {
                logger.warning("║    Error: " + entry.error);
            }
        }

        logger.info("╠══════════════════════════════════════════════════════╣");
        logger.info(String.format("║  Modules: %d enabled, %d failed | Total: %dms    ║",
                enabled, failed, totalTime));
        logger.info("╚══════════════════════════════════════════════════════╝");
    }

    private record ModuleEntry(String moduleId, ModuleState state, long durationMs, String error) {}
}
