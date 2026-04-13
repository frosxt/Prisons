package com.github.frosxt.prisoncore.spi.binding;

import java.util.UUID;

public interface MenuBinder {
    void open(UUID playerId, Object menuDescriptor);
    void close(UUID playerId);
}
