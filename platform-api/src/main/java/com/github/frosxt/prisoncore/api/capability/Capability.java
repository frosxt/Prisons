package com.github.frosxt.prisoncore.api.capability;

public interface Capability<T> {
    CapabilityKey<T> key();

    Class<T> contractType();
}
