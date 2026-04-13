package com.github.frosxt.prisoncore.command.api;

/**
 * Permission and sender-type gate applied to a command before its executor runs.
 * Use {@link #of(String)} for a permission-only gate, {@link #playerOnly(String)}
 * to also require a {@code Player} sender, or {@link #none()} for no gate.
 */
public final class PermissionPolicy {
    private final String node;
    private final boolean playerOnly;
    private final boolean consoleOnly;

    public PermissionPolicy(final String node, final boolean playerOnly, final boolean consoleOnly) {
        this.node = node;
        this.playerOnly = playerOnly;
        this.consoleOnly = consoleOnly;
    }

    public static PermissionPolicy none() {
        return new PermissionPolicy(null, false, false);
    }

    public static PermissionPolicy of(final String node) {
        return new PermissionPolicy(node, false, false);
    }

    public static PermissionPolicy playerOnly(final String node) {
        return new PermissionPolicy(node, true, false);
    }

    public String node() {
        return node;
    }

    public boolean playerOnly() {
        return playerOnly;
    }

    public boolean consoleOnly() {
        return consoleOnly;
    }
}
