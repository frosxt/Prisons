package com.github.frosxt.prisoncore.api.module;

public interface ModuleBootstrap {
    PlatformModule create(ModuleContext context);
}
