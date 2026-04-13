package com.github.frosxt.prisoncore.api.service;

/**
 * Lifetime of a service registered in the {@link ServiceContainer}.
 */
public enum ServiceScope {
    /** Lives for the entire kernel lifetime; shared across every module. */
    KERNEL,
    /** Created on module enable, disposed on module disable. */
    MODULE,
    /** Created on player join, disposed on player quit or kernel shutdown. */
    SESSION
}
