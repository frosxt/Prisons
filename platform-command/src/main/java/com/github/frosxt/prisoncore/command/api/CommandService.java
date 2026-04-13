package com.github.frosxt.prisoncore.command.api;

/**
 * Registers platform-defined commands. Modules resolve this from the service container
 * and register every command they own here instead of touching the Bukkit command map.
 */
public interface CommandService {

    /** Register a command; subcommands defined on the descriptor are exposed with it. */
    void register(CommandDescriptor descriptor);

    /** Remove a previously-registered command. Safe to call in {@code onDisable}. */
    void unregister(CommandKey key);
}
