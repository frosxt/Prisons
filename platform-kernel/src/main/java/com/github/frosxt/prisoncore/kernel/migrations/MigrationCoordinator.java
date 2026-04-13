package com.github.frosxt.prisoncore.kernel.migrations;

import com.github.frosxt.prisoncore.spi.migration.MigrationContext;
import com.github.frosxt.prisoncore.spi.migration.MigrationProvider;
import com.github.frosxt.prisoncore.spi.migration.ModuleMigration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MigrationCoordinator {
    private final Logger logger;
    private final Path versionsFile;
    private final Map<String, Integer> currentVersions = new LinkedHashMap<>();
    private final List<MigrationProvider> providers = new ArrayList<>();

    public MigrationCoordinator(final Logger logger, final Path dataFolder) {
        this.logger = logger;
        this.versionsFile = dataFolder.resolve("migrations").resolve("versions.properties");
        loadVersions();
    }

    public void addProvider(final MigrationProvider provider) {
        providers.add(provider);
    }

    public void setCurrentVersion(final String moduleId, final int version) {
        currentVersions.put(moduleId, version);
        saveVersions();
    }

    public int getCurrentVersion(final String moduleId) {
        return currentVersions.getOrDefault(moduleId, 0);
    }

    public int runMigrations(final String moduleId, final MigrationContext context) {
        final int currentVersion = getCurrentVersion(moduleId);

        final List<ModuleMigration> allMigrations = new ArrayList<>();
        for (final MigrationProvider provider : providers) {
            allMigrations.addAll(provider.migrations(moduleId));
        }

        allMigrations.sort(Comparator.comparingInt(ModuleMigration::fromVersion));

        final List<ModuleMigration> applicable = new ArrayList<>();
        int expectedFrom = currentVersion;
        for (final ModuleMigration migration : allMigrations) {
            if (migration.fromVersion() < currentVersion) {
                continue;
            }
            if (migration.fromVersion() == expectedFrom) {
                applicable.add(migration);
                expectedFrom = migration.toVersion();
            }
        }

        int chainVersion = currentVersion;
        for (final ModuleMigration migration : applicable) {
            if (migration.fromVersion() != chainVersion) {
                logger.severe("[PrisonCore] Migration chain gap for " + moduleId
                        + ": expected from v" + chainVersion
                        + " but next is from v" + migration.fromVersion());
                return 0;
            }
            chainVersion = migration.toVersion();
        }

        int applied = 0;
        for (final ModuleMigration migration : applicable) {
            try {
                logger.info("[PrisonCore] Running migration for " + moduleId
                        + ": v" + migration.fromVersion() + " -> v" + migration.toVersion());
                migration.apply(context);
                currentVersions.put(moduleId, migration.toVersion());
                saveVersions();
                applied++;
            } catch (final Exception e) {
                logger.log(Level.SEVERE, "[PrisonCore] Migration failed for " + moduleId
                        + " at v" + migration.fromVersion() + " -> v" + migration.toVersion(), e);
                return applied;
            }
        }

        if (!applicable.isEmpty()) {
            logger.info("[PrisonCore] Migrations complete for " + moduleId
                    + ": now at v" + getCurrentVersion(moduleId));
        }
        return applied;
    }

    private void loadVersions() {
        if (!Files.exists(versionsFile)) {
            return;
        }
        try (final BufferedReader reader = Files.newBufferedReader(versionsFile)) {
            final Properties props = new Properties();
            props.load(reader);
            for (final String key : props.stringPropertyNames()) {
                try {
                    currentVersions.put(key, Integer.parseInt(props.getProperty(key)));
                } catch (final NumberFormatException ignored) {
                    // ignored
                }
            }
        } catch (final IOException e) {
            logger.log(Level.WARNING, "[PrisonCore] Failed to load migration versions", e);
        }
    }

    private void saveVersions() {
        try {
            Files.createDirectories(versionsFile.getParent());
            final Properties props = new Properties();
            currentVersions.forEach((k, v) -> props.setProperty(k, String.valueOf(v)));

            try (final BufferedWriter writer = Files.newBufferedWriter(versionsFile)) {
                props.store(writer, "PrisonCore module schema versions");
            }
        } catch (final IOException e) {
            logger.log(Level.WARNING, "[PrisonCore] Failed to save migration versions", e);
        }
    }
}
