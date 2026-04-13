package com.github.frosxt.prisoncore.command.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Immutable description of a command: key, permission, executor, completion provider,
 * and any subcommands. Construct via {@link #builder(String, String)}.
 */
public final class CommandDescriptor {
    private final CommandKey key;
    private final List<String> aliases;
    private final PermissionPolicy permission;
    private final String description;
    private final CommandExecutor executor;
    private final CompletionProvider completionProvider;
    private final List<CommandDescriptor> subcommands;

    private CommandDescriptor(final Builder builder) {
        this.key = builder.key;
        this.aliases = Collections.unmodifiableList(builder.aliases);
        this.permission = builder.permission;
        this.description = builder.description;
        this.executor = builder.executor;
        this.completionProvider = builder.completionProvider;
        this.subcommands = Collections.unmodifiableList(builder.subcommands);
    }

    public static Builder builder(final String namespace, final String name) {
        return new Builder(new CommandKey(namespace, name));
    }

    public CommandKey key() {
        return key;
    }

    public List<String> aliases() {
        return aliases;
    }

    public PermissionPolicy permission() {
        return permission;
    }

    public String description() {
        return description;
    }

    public CommandExecutor executor() {
        return executor;
    }

    public CompletionProvider completionProvider() {
        return completionProvider;
    }

    public List<CommandDescriptor> subcommands() {
        return subcommands;
    }

    public static final class Builder {
        private final CommandKey key;
        private List<String> aliases = new ArrayList<>();
        private PermissionPolicy permission = PermissionPolicy.none();
        private String description = "";
        private CommandExecutor executor;
        private CompletionProvider completionProvider;
        private final List<CommandDescriptor> subcommands = new ArrayList<>();

        private Builder(final CommandKey key) {
            this.key = key;
        }

        public Builder aliases(final String... aliases) {
            this.aliases = Arrays.asList(aliases);
            return this;
        }

        public Builder permission(final PermissionPolicy permission) {
            this.permission = permission;
            return this;
        }

        public Builder description(final String description) {
            this.description = description;
            return this;
        }

        public Builder executor(final CommandExecutor executor) {
            this.executor = executor;
            return this;
        }

        public Builder completionProvider(final CompletionProvider provider) {
            this.completionProvider = provider;
            return this;
        }

        public Builder subcommand(final CommandDescriptor sub) {
            this.subcommands.add(sub);
            return this;
        }

        public CommandDescriptor build() {
            return new CommandDescriptor(this);
        }
    }
}
