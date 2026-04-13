package com.github.frosxt.prisoncore.command.api;

import java.util.UUID;

/**
 * Execution context passed to a {@link CommandExecutor}.
 * Sender is typed as {@code Object} to keep the core API platform-neutral;
 * Bukkit callers can cast to {@code CommandSender} safely.
 */
public final class CommandContext {
    private final Object sender;
    private final String label;
    private final String[] args;
    private final UUID senderId;

    public CommandContext(final Object sender, final String label, final String[] args, final UUID senderId) {
        this.sender = sender;
        this.label = label;
        this.args = args;
        this.senderId = senderId;
    }

    public Object sender() {
        return sender;
    }

    public String label() {
        return label;
    }

    public String[] args() {
        return args;
    }

    public UUID senderId() {
        return senderId;
    }

    /** @return the argument at {@code index}, or {@code null} if out of bounds. */
    public String arg(final int index) {
        return index < args.length ? args[index] : null;
    }

    public int argCount() {
        return args.length;
    }
}
