package com.github.frosxt.prisoncore.runtime.bukkit.admin;

import com.github.frosxt.prisoncore.api.lifecycle.LifecycleState;
import com.github.frosxt.prisoncore.api.module.ModuleDescriptor;
import com.github.frosxt.prisoncore.api.module.ModuleState;
import com.github.frosxt.prisoncore.api.platform.PlatformInfo;
import com.github.frosxt.prisoncore.command.api.*;
import com.github.frosxt.prisoncore.commons.bukkit.color.ColorTranslator;
import com.github.frosxt.prisoncore.kernel.Kernel;
import com.github.frosxt.prisoncore.kernel.module.DefaultModuleHandle;
import com.github.frosxt.prisoncore.kernel.module.DefaultModuleManager;
import com.github.frosxt.prisoncore.spi.module.ModuleCandidate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Core-managed administrative command component.
 *
 * Registered directly by {@code BukkitKernelBootstrap} after kernel activation.
 * It is loaded before any user module and cannot be disabled at runtime.
 */
public final class CoreAdminCommands {
    private static final String NAMESPACE = "prisoncore";
    private static final String ROOT_PERMISSION = "prisoncore.admin";
    private static final String MODULE_PERMISSION = "prisoncore.admin.module";
    private static final String MODULE_ENABLE_PERMISSION = "prisoncore.admin.module.enable";
    private static final String MODULE_DISABLE_PERMISSION = "prisoncore.admin.module.disable";
    private static final String MODULE_RELOAD_PERMISSION = "prisoncore.admin.module.reload";
    private static final String VERSION_PERMISSION = "prisoncore.admin.version";

    private static final String BORDER = "&6&m-----------------------------------------";
    private static final String BLANK_LINE = " ";
    private static final String LINE_PREFIX = "&e&lPrisons &8&l→ ";

    private final Kernel kernel;

    public CoreAdminCommands(final Kernel kernel) {
        this.kernel = kernel;
    }

    public void register() {
        final CommandService commandService = kernel.container().resolve(CommandService.class);
        commandService.register(buildRootCommand());
    }

    private CommandDescriptor buildRootCommand() {
        return CommandDescriptor.builder(NAMESPACE, "prisoncore")
                .aliases("prison", "pc", "core", "prisons", "pcore", "prisonscore")
                .permission(PermissionPolicy.of(ROOT_PERMISSION))
                .description("PrisonCore platform administration")
                .subcommand(buildHelpSubcommand())
                .subcommand(buildVersionSubcommand())
                .subcommand(buildModulesRoot())
                .executor(ctx -> new CommandResult.Success(buildHelpMessage()))
                .build();
    }

    private CommandDescriptor buildHelpSubcommand() {
        return CommandDescriptor.builder(NAMESPACE, "help")
                .permission(PermissionPolicy.of(ROOT_PERMISSION))
                .description("Show the PrisonCore help menu")
                .executor(ctx -> new CommandResult.Success(buildHelpMessage()))
                .build();
    }

    private String buildHelpMessage() {
        final List<String> lines = List.of(
                "&7<> = Required",
                "&7[] = Optional",
                BLANK_LINE,
                "&e&lCOMMANDS:",
                bullet("/prisoncore help"),
                bullet("/prisoncore version"),
                bullet("/prisoncore modules list"),
                bullet("/prisoncore modules info <id>"),
                bullet("/prisoncore modules enable <id>"),
                bullet("/prisoncore modules disable <id>"),
                bullet("/prisoncore modules reload <id>")
        );
        return box("PrisonCore", lines);
    }

    private CommandDescriptor buildVersionSubcommand() {
        return CommandDescriptor.builder(NAMESPACE, "version")
                .permission(PermissionPolicy.of(VERSION_PERMISSION))
                .description("Show PrisonCore platform version")
                .executor(ctx -> {
                    final PlatformInfo info = kernel.container().resolve(PlatformInfo.class);
                    final DefaultModuleManager manager = kernel.moduleManager();
                    final List<String> lines = List.of(
                            labeled("Platform Version", "&f" + info.version()),
                            labeled("Server Version", "&f" + info.serverVersion()),
                            labeled("Lifecycle State", "&" + stateColor(info.state()) + info.state().name()),
                            labeled("Loaded Modules", "&f" + manager.allHandles().size()),
                            labeled("Capabilities", "&f" + kernel.capabilityRegistry().size())
                    );
                    return new CommandResult.Success(box("Version", lines));
                })
                .build();
    }

    private CommandDescriptor buildModulesRoot() {
        return CommandDescriptor.builder(NAMESPACE, "modules")
                .permission(PermissionPolicy.of(MODULE_PERMISSION))
                .description("Module lifecycle management")
                .subcommand(buildModuleListSubcommand())
                .subcommand(buildModuleInfoSubcommand())
                .subcommand(buildModuleEnableSubcommand())
                .subcommand(buildModuleDisableSubcommand())
                .subcommand(buildModuleReloadSubcommand())
                .executor(ctx -> new CommandResult.Success(colorizeLines(List.of(
                        prefixed("&eUsage: &f/prisoncore modules <list|info|enable|disable|reload>")
                ))))
                .build();
    }

    private CommandDescriptor buildModuleListSubcommand() {
        return CommandDescriptor.builder(NAMESPACE, "list")
                .permission(PermissionPolicy.of(MODULE_PERMISSION))
                .description("List all discovered modules")
                .executor(ctx -> {
                    final DefaultModuleManager manager = kernel.moduleManager();
                    final List<ModuleCandidate> resolved = manager.resolvedOrder();
                    final List<String> lines = new ArrayList<>();
                    if (resolved.isEmpty()) {
                        lines.add("&7No modules discovered.");
                    } else {
                        for (final ModuleCandidate candidate : resolved) {
                            final ModuleDescriptor desc = candidate.descriptor();
                            final DefaultModuleHandle handle = manager.handle(desc.id());
                            final ModuleState state = handle != null ? handle.state() : ModuleState.DISCOVERED;
                            lines.add(moduleListEntry(desc, state));
                        }
                    }
                    return new CommandResult.Success(box("Loaded Modules", lines));
                })
                .build();
    }

    private CommandDescriptor buildModuleInfoSubcommand() {
        return CommandDescriptor.builder(NAMESPACE, "info")
                .permission(PermissionPolicy.of(MODULE_PERMISSION))
                .description("Show detailed metadata for a module")
                .completionProvider(moduleIdCompletionProvider())
                .executor(ctx -> {
                    if (ctx.argCount() == 0) {
                        return new CommandResult.Success(usageLine("/prisoncore modules info <id>"));
                    }
                    final String id = ctx.arg(0);
                    final DefaultModuleHandle handle = kernel.moduleManager().handle(id);
                    if (handle == null) {
                        return new CommandResult.Success(errorLine("Module not found: &f" + id));
                    }

                    final ModuleDescriptor desc = handle.descriptor();
                    final List<String> lines = List.of(
                            prefixed("&eID: &f" + desc.id()),
                            prefixed("&eName: &f" + desc.name()),
                            prefixed("&eVersion: &f" + desc.version()),
                            prefixed("&eAPI Version: &f" + desc.apiVersion()),
                            prefixed("&eState: &" + stateColor(handle.state()) + handle.state().name()),
                            prefixed("&eLoad Phase: &f" + desc.loadPhase().name()),
                            prefixed("&eMain: &f" + desc.mainClass()),
                            prefixed("&eRequired Dependencies: " + formatList(desc.requiredDependencies())),
                            prefixed("&eOptional Dependencies: " + formatList(desc.optionalDependencies())),
                            prefixed("&eProvides Capabilities: " + formatList(desc.providesCapabilities())),
                            prefixed("&eRequires Capabilities: " + formatList(desc.requiresCapabilities()))
                    );
                    return new CommandResult.Success(colorizeLines(lines));
                })
                .build();
    }

    private CommandDescriptor buildModuleEnableSubcommand() {
        return CommandDescriptor.builder(NAMESPACE, "enable")
                .permission(PermissionPolicy.of(MODULE_ENABLE_PERMISSION))
                .description("Enable a disabled module")
                .completionProvider(moduleIdCompletionProvider())
                .executor(ctx -> {
                    if (ctx.argCount() == 0) {
                        return new CommandResult.Success(usageLine("/prisoncore modules enable <id>"));
                    }
                    final String id = ctx.arg(0);
                    if (!kernel.moduleManager().enableById(id)) {
                        return new CommandResult.Success(errorLine("Failed to enable: &f" + id));
                    }
                    return new CommandResult.Success(successLine("Enabled module: &f" + id));
                })
                .build();
    }

    private CommandDescriptor buildModuleDisableSubcommand() {
        return CommandDescriptor.builder(NAMESPACE, "disable")
                .permission(PermissionPolicy.of(MODULE_DISABLE_PERMISSION))
                .description("Disable an enabled module")
                .completionProvider(moduleIdCompletionProvider())
                .executor(ctx -> {
                    if (ctx.argCount() == 0) {
                        return new CommandResult.Success(usageLine("/prisoncore modules disable <id>"));
                    }
                    final String id = ctx.arg(0);
                    if (!kernel.moduleManager().disableById(id)) {
                        return new CommandResult.Success(errorLine("Failed to disable: &f" + id));
                    }
                    return new CommandResult.Success(successLine("Disabled module: &f" + id));
                })
                .build();
    }

    private CommandDescriptor buildModuleReloadSubcommand() {
        return CommandDescriptor.builder(NAMESPACE, "reload")
                .permission(PermissionPolicy.of(MODULE_RELOAD_PERMISSION))
                .description("Reload a module (disable + enable)")
                .completionProvider(moduleIdCompletionProvider())
                .executor(ctx -> {
                    if (ctx.argCount() == 0) {
                        return new CommandResult.Success(usageLine("/prisoncore modules reload <id>"));
                    }
                    final String id = ctx.arg(0);
                    if (!kernel.moduleManager().reloadById(id)) {
                        return new CommandResult.Success(errorLine("Failed to reload: &f" + id));
                    }
                    return new CommandResult.Success(successLine("Reloaded module: &f" + id));
                })
                .build();
    }

    private CompletionProvider moduleIdCompletionProvider() {
        return (ctx, argIndex) -> {
            if (argIndex != 0) {
                return List.of();
            }
            final String partial = ctx.arg(0) != null ? ctx.arg(0).toLowerCase() : "";
            return kernel.moduleManager().resolvedOrder().stream()
                    .map(c -> c.descriptor().id())
                    .filter(id -> id.toLowerCase().startsWith(partial))
                    .toList();
        };
    }

    private static String box(final String title, final List<String> contentLines) {
        final StringBuilder sb = new StringBuilder();
        sb.append(BORDER).append('\n');
        sb.append(BLANK_LINE).append('\n');
        sb.append("&e&lPrisons &8&l→ &e").append(title).append('\n');
        sb.append(BLANK_LINE).append('\n');
        for (final String line : contentLines) {
            sb.append(line).append('\n');
        }
        sb.append(BLANK_LINE).append('\n');
        sb.append(BORDER);
        return ColorTranslator.colorize(sb.toString());
    }

    private static String bullet(final String text) {
        return "&e&l→ &7" + text;
    }

    private static String labeled(final String label, final String value) {
        return "&e&l→ &7" + label + ": " + value;
    }

    private static String prefixed(final String content) {
        return LINE_PREFIX + content;
    }

    private static String colorizeLines(final List<String> lines) {
        return ColorTranslator.colorize(String.join("\n", lines));
    }

    private static String successLine(final String message) {
        return ColorTranslator.colorize(LINE_PREFIX + "&a" + message);
    }

    private static String errorLine(final String message) {
        return ColorTranslator.colorize(LINE_PREFIX + "&c" + message);
    }

    private static String usageLine(final String usage) {
        return ColorTranslator.colorize(LINE_PREFIX + "&eUsage: &f" + usage);
    }

    private static String moduleListEntry(final ModuleDescriptor desc, final ModuleState state) {
        return "&e&l→ &f" + desc.name()
                + " &8v" + desc.version()
                + " &7[&" + stateColor(state) + state.name() + "&7]"
                + " &8(" + desc.id() + ")";
    }

    private static char stateColor(final ModuleState state) {
        return switch (state) {
            case ENABLED -> 'a';
            case FAILED -> 'c';
            case DISABLED, DISABLING -> '8';
            case PREPARED, RESOLVED, VALIDATED -> 'e';
            case DISCOVERED -> '7';
        };
    }

    private static char stateColor(final LifecycleState state) {
        return switch (state) {
            case ACTIVE -> 'a';
            case DISABLED -> 'c';
            case QUIESCING -> '6';
            default -> 'e';
        };
    }

    private static String formatList(final Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return "&8(none)";
        }
        return "&f" + String.join("&7, &f", values);
    }
}
