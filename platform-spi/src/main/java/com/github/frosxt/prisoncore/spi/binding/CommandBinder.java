package com.github.frosxt.prisoncore.spi.binding;

public interface CommandBinder {
    void bind(Object commandDescriptor);
    void unbind(String commandName);
}
