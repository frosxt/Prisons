package com.github.frosxt.prisoncore.message.core;

import com.github.frosxt.prisoncore.message.api.model.ConfiguredMessage;
import com.github.frosxt.prisoncore.message.api.MessageKey;
import com.github.frosxt.prisoncore.message.api.model.PlatformMessages;
import com.github.frosxt.prisoncore.message.api.channel.ActionBarChannel;
import com.github.frosxt.prisoncore.message.api.channel.ChatChannel;
import com.github.frosxt.prisoncore.message.api.channel.SoundChannel;
import com.github.frosxt.prisoncore.message.api.channel.TitleChannel;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MessageDefinitionRegistry {

    private final Map<String, ConfiguredMessage> defaults = new ConcurrentHashMap<>();

    public MessageDefinitionRegistry() {
        registerPlatformDefaults();
    }

    public void register(final ConfiguredMessage message) {
        defaults.put(message.key().qualifiedKey(), message);
    }

    public ConfiguredMessage getDefault(final MessageKey key) {
        return defaults.get(key.qualifiedKey());
    }

    private void registerPlatformDefaults() {
        chatOnly(PlatformMessages.COMMAND_PLAYER_ONLY,
                "This command can only be used by players.");
        chatOnly(PlatformMessages.COMMAND_CONSOLE_ONLY,
                "This command can only be used from console.");
        chatWithSound(PlatformMessages.COMMAND_NO_PERMISSION,
                "&cYou do not have permission to use this command.",
                "entity.villager.no", 1.0f, 1.0f);
        chatOnly(PlatformMessages.SUBCOMMAND_PLAYER_ONLY,
                "This subcommand can only be used by players.");
        chatOnly(PlatformMessages.SUBCOMMAND_CONSOLE_ONLY,
                "This subcommand can only be used from console.");
        chatOnly(PlatformMessages.COMMAND_ERROR, "&c{error}");
        chatOnly(PlatformMessages.COMMAND_USAGE, "&eUsage: {usage}");
    }

    private void chatOnly(final MessageKey key, final String text) {
        defaults.put(key.qualifiedKey(), new ConfiguredMessage(
                key,
                new ChatChannel(true, List.of(text)),
                ActionBarChannel.disabled(),
                TitleChannel.disabled(),
                SoundChannel.disabled()
        ));
    }

    private void chatWithSound(final MessageKey key, final String text,
                                final String sound, final float volume, final float pitch) {
        defaults.put(key.qualifiedKey(), new ConfiguredMessage(
                key,
                new ChatChannel(true, List.of(text)),
                ActionBarChannel.disabled(),
                TitleChannel.disabled(),
                new SoundChannel(true, sound, volume, pitch)
        ));
    }
}
