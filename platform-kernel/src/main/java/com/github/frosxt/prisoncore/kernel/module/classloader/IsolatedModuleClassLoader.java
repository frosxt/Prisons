package com.github.frosxt.prisoncore.kernel.module.classloader;

import java.net.URL;
import java.net.URLClassLoader;

public final class IsolatedModuleClassLoader extends URLClassLoader {
    private static final String[] DELEGATED_PREFIXES = {
        "com.github.frosxt.prisoncore.api.",
        "com.github.frosxt.prisoncore.spi.",
        "com.github.frosxt.prisoncore.commons.api."
    };

    public IsolatedModuleClassLoader(final URL[] urls, final ClassLoader parent) {
        super(urls, parent);
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

        try {
            Class<?> c = findLoadedClass(name);
            if (c != null) {
                return c;
            }
            c = findClass(name);
            if (resolve) {
                resolveClass(c);
            }
            return c;
        } catch (final ClassNotFoundException e) {
            return super.loadClass(name, resolve);
        }
    }
}
