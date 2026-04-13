package com.github.frosxt.prisoncore.kernel.observability;

import com.github.frosxt.prisoncore.api.module.ModuleHandle;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class ModuleStatusReport {

    public static void print(final Logger logger, final Iterable<? extends ModuleHandle> handles) {
        final List<ModuleHandle> enabled = new ArrayList<>();
        final List<ModuleHandle> failed = new ArrayList<>();
        final List<ModuleHandle> disabled = new ArrayList<>();

        for (final ModuleHandle handle : handles) {
            switch (handle.state()) {
                case ENABLED -> enabled.add(handle);
                case FAILED -> failed.add(handle);
                case DISABLED -> disabled.add(handle);
                default -> disabled.add(handle);
            }
        }

        logger.info("=== Module Status ===");
        logger.info("Enabled (" + enabled.size() + "):");
        for (final ModuleHandle h : enabled) {
            logger.info("  + " + h.descriptor());
        }
        if (!failed.isEmpty()) {
            logger.info("Failed (" + failed.size() + "):");
            for (final ModuleHandle h : failed) {
                logger.warning("  ! " + h.descriptor());
            }
        }
        if (!disabled.isEmpty()) {
            logger.info("Disabled (" + disabled.size() + "):");
            for (final ModuleHandle h : disabled) {
                logger.info("  - " + h.descriptor());
            }
        }
    }
}
