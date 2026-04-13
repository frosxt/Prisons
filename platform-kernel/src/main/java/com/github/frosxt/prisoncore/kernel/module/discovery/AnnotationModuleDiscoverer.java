package com.github.frosxt.prisoncore.kernel.module.discovery;

import com.github.frosxt.prisoncore.api.module.ModuleDescriptor;
import com.github.frosxt.prisoncore.api.module.annotation.ModuleDefinition;
import com.github.frosxt.prisoncore.api.module.annotation.ModuleDefinitions;
import com.github.frosxt.prisoncore.kernel.module.classloader.IsolatedModuleClassLoader;
import com.github.frosxt.prisoncore.spi.module.ModuleCandidate;
import com.github.frosxt.prisoncore.spi.module.ModuleDiscoverer;

import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AnnotationModuleDiscoverer implements ModuleDiscoverer {
    private static final String MANIFEST_ATTRIBUTE = "PrisonCore-Bootstrap";

    private final Logger logger;

    public AnnotationModuleDiscoverer(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public List<ModuleCandidate> discover(final Path modulesDir) {
        final List<ModuleCandidate> candidates = new ArrayList<>();

        if (!Files.isDirectory(modulesDir)) {
            logger.info("[PrisonCore] Modules directory does not exist: " + modulesDir);
            try {
                Files.createDirectories(modulesDir);
            } catch (final IOException e) {
                logger.log(Level.WARNING, "[PrisonCore] Failed to create modules directory", e);
            }
            return candidates;
        }

        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(modulesDir, "*.jar")) {
            for (final Path jar : stream) {
                try {
                    final ModuleCandidate candidate = parseJar(jar);
                    if (candidate != null) {
                        candidates.add(candidate);
                        logger.info("[PrisonCore] Discovered module: " + candidate.descriptor());
                    }
                } catch (final Exception e) {
                    logger.log(Level.WARNING,
                            "[PrisonCore] Failed to parse module jar: " + jar.getFileName(), e);
                }
            }
        } catch (final IOException e) {
            logger.log(Level.SEVERE, "[PrisonCore] Failed to scan modules directory", e);
        }

        return candidates;
    }

    private ModuleCandidate parseJar(final Path jarPath) throws Exception {
        final String bootstrapClassName;

        try (final JarFile jar = new JarFile(jarPath.toFile())) {
            final Manifest manifest = jar.getManifest();
            if (manifest == null) {
                logger.fine("[PrisonCore] No manifest found in: " + jarPath.getFileName());
                return null;
            }

            bootstrapClassName = manifest.getMainAttributes().getValue(MANIFEST_ATTRIBUTE);
            if (bootstrapClassName == null) {
                logger.fine("[PrisonCore] No " + MANIFEST_ATTRIBUTE
                        + " attribute in: " + jarPath.getFileName());
                return null;
            }
        }

        final URL jarUrl = jarPath.toUri().toURL();
        try (final IsolatedModuleClassLoader tempLoader =
                     new IsolatedModuleClassLoader(new URL[]{jarUrl}, getClass().getClassLoader())) {
            final Class<?> bootstrapClass = tempLoader.loadClass(bootstrapClassName);
            final ModuleDefinition definition = bootstrapClass.getAnnotation(ModuleDefinition.class);

            if (definition == null) {
                logger.warning("[PrisonCore] Bootstrap class " + bootstrapClassName
                        + " is missing @ModuleDefinition annotation in: " + jarPath.getFileName());
                return null;
            }

            final ModuleDescriptor descriptor = ModuleDefinitions.fromAnnotation(
                    definition, bootstrapClassName);
            return new ModuleCandidate(jarPath, descriptor);
        }
    }
}
