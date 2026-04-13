package com.github.frosxt.prisoncore.command.api;

/**
 * Return value from a {@link CommandExecutor}. The command service uses this
 * to drive message delivery and Bukkit command return codes.
 */
public sealed interface CommandResult {
    /** Command completed successfully. Message is optional (null for no feedback). */
    record Success(String message) implements CommandResult {
    }
    /** Command failed for a reason that is not an argument error. */
    record Error(String message) implements CommandResult {
    }
    /** Caller's arguments were malformed. Shown to the sender as usage help. */
    record Usage(String usage) implements CommandResult {
    }
}
