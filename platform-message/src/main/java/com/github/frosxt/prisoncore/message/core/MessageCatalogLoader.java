package com.github.frosxt.prisoncore.message.core;

import com.github.frosxt.prisoncore.message.api.model.ConfiguredMessage;
import com.github.frosxt.prisoncore.message.api.MessageKey;
import com.github.frosxt.prisoncore.message.api.channel.ActionBarChannel;
import com.github.frosxt.prisoncore.message.api.channel.ChatChannel;
import com.github.frosxt.prisoncore.message.api.channel.SoundChannel;
import com.github.frosxt.prisoncore.message.api.channel.TitleChannel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public final class MessageCatalogLoader {

    private final Logger logger;

    public MessageCatalogLoader(final Logger logger) {
        this.logger = logger;
    }

    @SuppressWarnings("unchecked")
    public ConfiguredMessage load(final String qualifiedKey, final Map<String, Object> section) {
        final int lastSep = qualifiedKey.lastIndexOf('-');
        final String namespace = lastSep > 0 ? qualifiedKey.substring(0, lastSep) : "platform";
        final String key = lastSep > 0 ? qualifiedKey.substring(lastSep + 1) : qualifiedKey;
        final MessageKey messageKey = MessageKey.of(namespace, key);

        ChatChannel chat = ChatChannel.disabled();
        ActionBarChannel actionBar = ActionBarChannel.disabled();
        TitleChannel title = TitleChannel.disabled();
        SoundChannel sound = SoundChannel.disabled();

        final Object chatObj = section.get("chat");
        if (chatObj instanceof Map) {
            chat = parseChat((Map<String, Object>) chatObj);
        }

        final Object abObj = section.get("action-bar");
        if (abObj instanceof Map) {
            actionBar = parseActionBar((Map<String, Object>) abObj);
        }

        final Object titleObj = section.get("title");
        if (titleObj instanceof Map) {
            title = parseTitle((Map<String, Object>) titleObj);
        }

        final Object soundObj = section.get("sound");
        if (soundObj instanceof Map) {
            sound = parseSound((Map<String, Object>) soundObj);
        }

        return new ConfiguredMessage(messageKey, chat, actionBar, title, sound);
    }

    @SuppressWarnings("unchecked")
    private ChatChannel parseChat(final Map<String, Object> section) {
        final boolean enabled = getBool(section, "enabled", true);
        final Object value = section.get("value");
        final List<String> lines;
        if (value instanceof List) {
            lines = new ArrayList<>();
            for (final Object item : (List<?>) value) {
                lines.add(item != null ? item.toString() : "");
            }
        } else if (value != null) {
            lines = List.of(value.toString());
        } else {
            lines = Collections.emptyList();
        }
        return new ChatChannel(enabled, lines);
    }

    private ActionBarChannel parseActionBar(final Map<String, Object> section) {
        return new ActionBarChannel(
                getBool(section, "enabled", false),
                getStr(section, "value", "")
        );
    }

    private TitleChannel parseTitle(final Map<String, Object> section) {
        return new TitleChannel(
                getBool(section, "enabled", false),
                getStr(section, "title", ""),
                getStr(section, "subtitle", ""),
                getInt(section, "fade-in", 10),
                getInt(section, "stay", 70),
                getInt(section, "fade-out", 20)
        );
    }

    private SoundChannel parseSound(final Map<String, Object> section) {
        return new SoundChannel(
                getBool(section, "enabled", false),
                getStr(section, "value", ""),
                getFloat(section, "volume", 1.0f),
                getFloat(section, "pitch", 1.0f)
        );
    }

    private static boolean getBool(final Map<String, Object> m, final String k, final boolean d) {
        final Object v = m.get(k);
        if (v instanceof Boolean) {
            return (Boolean) v;
        }
        return v != null ? Boolean.parseBoolean(v.toString()) : d;
    }

    private static String getStr(final Map<String, Object> m, final String k, final String d) {
        final Object v = m.get(k);
        return v != null ? v.toString() : d;
    }

    private static int getInt(final Map<String, Object> m, final String k, final int d) {
        final Object v = m.get(k);
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        if (v != null) {
            try { return Integer.parseInt(v.toString()); } catch (final NumberFormatException ignored) {}
        }
        return d;
    }

    private static float getFloat(final Map<String, Object> m, final String k, final float d) {
        final Object v = m.get(k);
        if (v instanceof Number) {
            return ((Number) v).floatValue();
        }
        if (v != null) {
            try { return Float.parseFloat(v.toString()); } catch (final NumberFormatException ignored) {}
        }
        return d;
    }
}
