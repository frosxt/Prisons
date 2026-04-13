package com.github.frosxt.prisoncore.api.module;

public interface ModuleHandle {
    ModuleDescriptor descriptor();

    ModuleState state();

    ClassLoader classLoader();
}
