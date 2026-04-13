package com.github.frosxt.prisoncore.api.module.annotation;

import com.github.frosxt.prisoncore.api.module.ModuleDescriptor;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ModuleDefinitions {
    private ModuleDefinitions() {
        throw new UnsupportedOperationException("Utility classes cannot be instantiated");
    }

    public static ModuleDescriptor fromAnnotation(final ModuleDefinition annotation, final String bootstrapClassName) {
        final List<String> requiredDeps = Arrays.asList(annotation.requiredDependencies());
        final List<String> optionalDeps = Arrays.asList(annotation.optionalDependencies());
        final Set<String> providesCaps = new LinkedHashSet<>(Arrays.asList(annotation.providesCapabilities()));
        final Set<String> requiresCaps = new LinkedHashSet<>(Arrays.asList(annotation.requiresCapabilities()));

        return new ModuleDescriptor(
                annotation.id(),
                annotation.name(),
                annotation.version(),
                annotation.apiVersion(),
                bootstrapClassName,
                annotation.loadPhase(),
                requiredDeps,
                optionalDeps,
                providesCaps,
                requiresCaps
        );
    }
}
