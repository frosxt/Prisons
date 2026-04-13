package com.github.frosxt.prisoncore.spi.module;

public interface ModuleClassLoaderFactory {
    ClassLoader create(ModuleCandidate candidate, ClassLoader parent);
}
