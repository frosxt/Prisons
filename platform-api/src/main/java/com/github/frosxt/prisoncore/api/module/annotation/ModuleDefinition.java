package com.github.frosxt.prisoncore.api.module.annotation;

import com.github.frosxt.prisoncore.api.module.ModuleLoadPhase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a class as the bootstrap entry point for a platform module.
 * The kernel scans loaded module jars for a class carrying this annotation,
 * instantiates it, and drives it through the module lifecycle.
 *
 * <p>Module identity (id, version, dependencies, capabilities, load phase)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModuleDefinition {
    String id();

    String name();

    String version();

    String apiVersion() default "1.0";

    ModuleLoadPhase loadPhase() default ModuleLoadPhase.POST_INFRASTRUCTURE;

    String[] requiredDependencies() default {};

    String[] optionalDependencies() default {};

    String[] providesCapabilities() default {};

    String[] requiresCapabilities() default {};
}
