package com.github.frosxt.prisoncore.command.api;

@FunctionalInterface
public interface CommandExecutor {
    CommandResult execute(CommandContext context);
}
