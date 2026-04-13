package com.github.frosxt.prisoncore.message.api.model;

import com.github.frosxt.prisoncore.message.api.MessageKey;

public final class PlatformMessages {

    public static final MessageKey COMMAND_PLAYER_ONLY =
            MessageKey.of("platform-command", "player_only");
    public static final MessageKey COMMAND_CONSOLE_ONLY =
            MessageKey.of("platform-command", "console_only");
    public static final MessageKey COMMAND_NO_PERMISSION =
            MessageKey.of("platform-command", "no_permission");
    public static final MessageKey SUBCOMMAND_PLAYER_ONLY =
            MessageKey.of("platform-command", "subcommand_player_only");
    public static final MessageKey SUBCOMMAND_CONSOLE_ONLY =
            MessageKey.of("platform-command", "subcommand_console_only");
    public static final MessageKey COMMAND_ERROR =
            MessageKey.of("platform-command", "error");
    public static final MessageKey COMMAND_USAGE =
            MessageKey.of("platform-command", "usage");

    private PlatformMessages() {
    }
}
