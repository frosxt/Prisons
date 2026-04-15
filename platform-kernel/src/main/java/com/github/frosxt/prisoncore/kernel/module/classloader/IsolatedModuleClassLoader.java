package com.github.frosxt.prisoncore.kernel.module.classloader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;

public final class IsolatedModuleClassLoader extends URLClassLoader {
    private static final String[] DELEGATED_PREFIXES = {
        "com.github.frosxt.prisoncore.api.",
        "com.github.frosxt.prisoncore.spi.",
        "com.github.frosxt.prisoncore.commons.api."
    };

    private final List<ClassLoader> dependencyLoaders;

    public IsolatedModuleClassLoader(final URL[] urls, final ClassLoader parent) {
        this(urls, parent, Collections.emptyList());
    }

    public IsolatedModuleClassLoader(final URL[] urls, final ClassLoader parent, final List<ClassLoader> dependencyLoaders) {
        super(urls, parent);
        this.dependencyLoaders = List.copyOf(dependencyLoaders);
    }

    @Override
    protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        if (name.startsWith("java.") || name.startsWith("javax.")) {
            return super.loadClass(name, resolve);
        }

        for (final String prefix : DELEGATED_PREFIXES) {
            if (name.startsWith(prefix)) {
                return getParent().loadClass(name);
            }
        }

        if (name.startsWith("org.bukkit.") || name.startsWith("net.minecraft.") || name.startsWith("org.spigotmc.")) {
            return getParent().loadClass(name);
        }

        final Class<?> already = findLoadedClass(name);
        if (already != null) {
            if (resolve) {
                resolveClass(already);
            }
            return already;
        }

        try {
            final Class<?> own = findClass(name);
            if (resolve) {
                resolveClass(own);
            }
            return own;
        } catch (final ClassNotFoundException ignored) {
        }

        for (final ClassLoader dep : dependencyLoaders) {
            try {
                final Class<?> shared = dep.loadClass(name);
                if (resolve) {
                    resolveClass(shared);
                }
                return shared;
            } catch (final ClassNotFoundException ignored) {
            }
        }

        return super.loadClass(name, resolve);
    }
}
