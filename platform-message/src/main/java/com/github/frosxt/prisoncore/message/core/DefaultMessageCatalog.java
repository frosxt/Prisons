package com.github.frosxt.prisoncore.message.core;

import com.github.frosxt.prisoncore.config.api.ConfigService;
import com.github.frosxt.prisoncore.message.api.model.ConfiguredMessage;
import com.github.frosxt.prisoncore.message.api.MessageCatalog;
import com.github.frosxt.prisoncore.message.api.MessageKey;
import com.github.frosxt.prisoncore.message.api.model.ResolvedMessage;
import com.github.frosxt.prisoncore.message.api.channel.ActionBarChannel;
import com.github.frosxt.prisoncore.message.api.channel.ChatChannel;
import com.github.frosxt.prisoncore.message.api.channel.SoundChannel;
import com.github.frosxt.prisoncore.message.api.channel.TitleChannel;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class DefaultMessageCatalog implements MessageCatalog {

    private static final String CONFIG_NAME = "messages";

    private final ConfigService configService;
    private final MessageDefinitionRegistry defaults;
    private final MessageCatalogLoader loader;
    private final Map<String, ConfiguredMessage> loaded = new ConcurrentHashMap<>();

    public DefaultMessageCatalog(final ConfigService configService,
                                  final MessageDefinitionRegistry defaults,
                                  final Logger logger) {
        this.configService = configService;
        this.defaults = defaults;
        this.loader = new MessageCatalogLoader(logger);
        loadFromConfig();
    }

    @Override
    public ConfiguredMessage resolve(final MessageKey key) {
        final ConfiguredMessage fromConfig = loaded.get(key.qualifiedKey());
        if (fromConfig != null) {
            return fromConfig;
        }
        final ConfiguredMessage fromDefaults = defaults.getDefault(key);
        if (fromDefaults != null) {
            return fromDefaults;
        }
        return new ConfiguredMessage(key,
                new ChatChannel(true, List.of("&cMissing message: " + key.qualifiedKey())),
                ActionBarChannel.disabled(), TitleChannel.disabled(), SoundChannel.disabled());
    }

    @Override
    public ResolvedMessage resolve(final MessageKey key, final Map<String, String> replacements) {
        return ResolvedMessage.from(resolve(key), replacements);
    }

    @Override
    public void reload() {
        configService.reload(CONFIG_NAME);
        loaded.clear();
        loadFromConfig();
    }

    @Override
    public void registerDefault(final ConfiguredMessage message) {
        defaults.register(message);
    }

    @SuppressWarnings("unchecked")
    private void loadFromConfig() {
        final Map<String, Object> data = configService.load(CONFIG_NAME);
        final Object messagesSection = data.get("messages");
        if (messagesSection instanceof Map) {
            final Map<String, Object> messages = (Map<String, Object>) messagesSection;
            for (final Map.Entry<String, Object> entry : messages.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    final ConfiguredMessage msg = loader.load(
                            entry.getKey(), (Map<String, Object>) entry.getValue());
                    loaded.put(msg.key().qualifiedKey(), msg);
                }
            }
        }
    }
}
