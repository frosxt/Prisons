package com.github.frosxt.prisoncore.kernel.module.classloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

    @Override
    public URL getResource(final String name) {
        final URL local = findResource(name);
        if (local != null) {
            return local;
        }
        for (final ClassLoader dep : dependencyLoaders) {
            final URL shared = dep.getResource(name);
            if (shared != null) {
                return shared;
            }
        }
        final ClassLoader parent = getParent();
        if (parent != null) {
            return parent.getResource(name);
        }
        return null;
    }

    @Override
    public InputStream getResourceAsStream(final String name) {
        final URL resource = getResource(name);
        if (resource == null) {
            return null;
        }
        try {
            return resource.openStream();
        } catch (final IOException ex) {
            return null;
        }
    }

    @Override
    public Enumeration<URL> getResources(final String name) throws IOException {
        final Set<URL> combined = new LinkedHashSet<>();
        final Enumeration<URL> own = findResources(name);
        while (own.hasMoreElements()) {
            combined.add(own.nextElement());
        }
        for (final ClassLoader dep : dependencyLoaders) {
            final Enumeration<URL> depResources = dep.getResources(name);
            while (depResources.hasMoreElements()) {
                combined.add(depResources.nextElement());
            }
        }
        final ClassLoader parent = getParent();
        if (parent != null) {
            final Enumeration<URL> parentResources = parent.getResources(name);
            while (parentResources.hasMoreElements()) {
                combined.add(parentResources.nextElement());
            }
        }
        return Collections.enumeration(combined);
    }
}
