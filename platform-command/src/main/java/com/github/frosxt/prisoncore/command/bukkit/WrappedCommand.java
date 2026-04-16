package com.github.frosxt.prisoncore.command.bukkit;

import com.github.frosxt.prisoncore.command.api.CommandContext;
import com.github.frosxt.prisoncore.command.api.CommandDescriptor;
import com.github.frosxt.prisoncore.command.api.CommandResult;
import com.github.frosxt.prisoncore.command.api.PermissionPolicy;
import com.github.frosxt.prisoncore.commons.bukkit.color.ColorTranslator;
import com.github.frosxt.prisoncore.message.api.MessageService;
import com.github.frosxt.prisoncore.message.api.model.PlatformMessages;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class WrappedCommand extends BukkitCommand {

    private final CommandDescriptor descriptor;
    private final MessageService messageService;

    WrappedCommand(final CommandDescriptor descriptor, final MessageService messageService) {
        super(descriptor.key().name(), descriptor.description(),
                "/" + descriptor.key().name(), descriptor.aliases());
        this.descriptor = descriptor;
        this.messageService = messageService;
        if (descriptor.permission().node() != null) {
            setPermission(descriptor.permission().node());
        }
    }

    @Override
    public boolean execute(final CommandSender sender, final String label, final String[] args) {
        if (!enforceSenderPolicy(sender, descriptor.permission(), true)) {
            return true;
        }

        CommandDescriptor current = descriptor;
        int consumed = 0;

        while (consumed < args.length && !current.subcommands().isEmpty()) {
            final CommandDescriptor match = findSubcommand(current, args[consumed]);
            if (match == null) {
                break;
            }
            if (!enforceSubcommandPolicy(sender, match.permission())) {
                return true;
            }
            current = match;
            consumed++;
        }

        if (current.executor() == null) {
            return true;
        }

        final String[] remainingArgs = Arrays.copyOfRange(args, consumed, args.length);
        final UUID senderId = sender instanceof final Player p ? p.getUniqueId() : null;
        final CommandContext ctx = new CommandContext(sender, label, remainingArgs, senderId);
        final CommandResult result = current.executor().execute(ctx);
        handleResult(sender, result);
        return true;
    }

    @Override
    public List<String> tabComplete(final CommandSender sender, final String alias, final String[] args) {
        if (args.length == 0) {
            return Collections.emptyList();
        }

        CommandDescriptor current = descriptor;
        int consumed = 0;

        for (int i = 0; i < args.length - 1; i++) {
            final CommandDescriptor match = findSubcommand(current, args[i]);
            if (match == null) {
                break;
            }
            if (!isVisibleTo(match, sender)) {
                return Collections.emptyList();
            }
            current = match;
            consumed++;
        }

        final String partial = args[args.length - 1].toLowerCase();
        final int argIndexInCurrent = args.length - 1 - consumed;

        if (argIndexInCurrent == 0 && !current.subcommands().isEmpty()) {
            final List<String> completions = new ArrayList<>();
            for (final CommandDescriptor sub : current.subcommands()) {
                if (!isVisibleTo(sub, sender)) {
                    continue;
                }
                if (sub.key().name().toLowerCase().startsWith(partial)) {
                    completions.add(sub.key().name());
                }
            }
            if (!completions.isEmpty()) {
                return completions;
            }
        }

        if (current.completionProvider() != null) {
            final UUID senderId = sender instanceof final Player p ? p.getUniqueId() : null;
            final String[] scopedArgs = Arrays.copyOfRange(args, consumed, args.length);
            final CommandContext ctx = new CommandContext(sender, alias, scopedArgs, senderId);
            return current.completionProvider().complete(ctx, argIndexInCurrent);
        }

        return Collections.emptyList();
    }

    private CommandDescriptor findSubcommand(final CommandDescriptor parent, final String name) {
        for (final CommandDescriptor sub : parent.subcommands()) {
            if (sub.key().name().equalsIgnoreCase(name)) {
                return sub;
            }
        }
        return null;
    }

    private boolean enforceSenderPolicy(final CommandSender sender, final PermissionPolicy perm, final boolean topLevel) {
        if (perm.playerOnly() && !(sender instanceof Player)) {
            messageService.send(sender, topLevel
                    ? PlatformMessages.COMMAND_PLAYER_ONLY
                    : PlatformMessages.SUBCOMMAND_PLAYER_ONLY);
            return false;
        }
        if (perm.consoleOnly() && sender instanceof Player) {
            messageService.send(sender, topLevel
                    ? PlatformMessages.COMMAND_CONSOLE_ONLY
                    : PlatformMessages.SUBCOMMAND_CONSOLE_ONLY);
            return false;
        }
        return true;
    }

    private boolean enforceSubcommandPolicy(final CommandSender sender, final PermissionPolicy perm) {
        if (perm.node() != null && !sender.hasPermission(perm.node())) {
            messageService.send(sender, PlatformMessages.COMMAND_NO_PERMISSION);
            return false;
        }
        return enforceSenderPolicy(sender, perm, false);
    }

    private boolean isVisibleTo(final CommandDescriptor subcommand, final CommandSender sender) {
        final PermissionPolicy perm = subcommand.permission();
        if (perm.node() != null && !sender.hasPermission(perm.node())) {
            return false;
        }
        if (perm.playerOnly() && !(sender instanceof Player)) {
            return false;
        }
        return !perm.consoleOnly() || !(sender instanceof Player);
    }

    private void handleResult(final CommandSender sender, final CommandResult result) {
        if (result instanceof final CommandResult.Success s) {
            if (s.message() != null) {
                sender.sendMessage(ColorTranslator.colorize(s.message()));
            }
        } else if (result instanceof final CommandResult.Error e) {
            messageService.send(sender, PlatformMessages.COMMAND_ERROR,
                    Map.of("error", e.message()));
        } else if (result instanceof final CommandResult.Usage u) {
            messageService.send(sender, PlatformMessages.COMMAND_USAGE,
                    Map.of("usage", u.usage()));
        }
    }
}
