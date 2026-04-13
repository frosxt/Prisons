package com.github.frosxt.prisoncore.command.bukkit;

import com.github.frosxt.prisoncore.command.api.CommandDescriptor;
import com.github.frosxt.prisoncore.command.api.CommandKey;
import com.github.frosxt.prisoncore.command.api.CommandService;
import com.github.frosxt.prisoncore.message.api.MessageService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BukkitCommandService implements CommandService {

    private final Map<CommandKey, CommandDescriptor> commands = new ConcurrentHashMap<>();
    private final Map<CommandKey, WrappedCommand> registeredCommands = new ConcurrentHashMap<>();
    private final MessageService messageService;
    private CommandMap commandMap;

    public BukkitCommandService(final MessageService messageService) {
        this.messageService = messageService;
        try {
            final Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            f.setAccessible(true);
            this.commandMap = (CommandMap) f.get(Bukkit.getServer());
        } catch (final Exception e) {
            // Fallback
        }
    }

    @Override
    public void register(final CommandDescriptor descriptor) {
        commands.put(descriptor.key(), descriptor);
        if (commandMap != null) {
            final WrappedCommand wrapped = new WrappedCommand(descriptor, messageService);
            registeredCommands.put(descriptor.key(), wrapped);
            commandMap.register(descriptor.key().namespace(), wrapped);
        }
    }

    @Override
    public void unregister(final CommandKey key) {
        commands.remove(key);
        final WrappedCommand wrapped = registeredCommands.remove(key);
        if (wrapped != null && commandMap != null) {
            wrapped.unregister(commandMap);
            try {
                final Field knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
                knownCommandsField.setAccessible(true);
                @SuppressWarnings("unchecked")
                final Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);
                knownCommands.values().removeIf(cmd -> cmd == wrapped);
            } catch (final Exception ignored) {
                // ignored
            }
        }
    }
}
