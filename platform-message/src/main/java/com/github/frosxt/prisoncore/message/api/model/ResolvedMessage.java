package com.github.frosxt.prisoncore.message.api.model;

import com.github.frosxt.prisoncore.message.api.MessageKey;
import com.github.frosxt.prisoncore.message.api.channel.ActionBarChannel;
import com.github.frosxt.prisoncore.message.api.channel.ChatChannel;
import com.github.frosxt.prisoncore.message.api.channel.SoundChannel;
import com.github.frosxt.prisoncore.message.api.channel.TitleChannel;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ResolvedMessage {

    private final MessageKey key;
    private final ChatChannel chat;
    private final ActionBarChannel actionBar;
    private final TitleChannel title;
    private final SoundChannel sound;

    private ResolvedMessage(final MessageKey key, final ChatChannel chat,
                            final ActionBarChannel actionBar, final TitleChannel title,
                            final SoundChannel sound) {
        this.key = key;
        this.chat = chat;
        this.actionBar = actionBar;
        this.title = title;
        this.sound = sound;
    }

    public static ResolvedMessage from(final ConfiguredMessage configured,
                                        final Map<String, String> replacements) {
        if (replacements == null || replacements.isEmpty()) {
            return new ResolvedMessage(
                    configured.key(),
                    configured.chat(),
                    configured.actionBar(),
                    configured.title(),
                    configured.sound()
            );
        }

        final ChatChannel chat = configured.chat().enabled()
                ? new ChatChannel(true, applyReplacements(configured.chat().lines(), replacements))
                : configured.chat();

        final ActionBarChannel actionBar = configured.actionBar().enabled()
                ? new ActionBarChannel(true,
                    applyReplacements(configured.actionBar().value(), replacements))
                : configured.actionBar();

        final TitleChannel title = configured.title().enabled()
                ? new TitleChannel(true,
                    applyReplacements(configured.title().title(), replacements),
                    applyReplacements(configured.title().subtitle(), replacements),
                    configured.title().fadeIn(), configured.title().stay(), configured.title().fadeOut())
                : configured.title();

        return new ResolvedMessage(configured.key(), chat, actionBar, title, configured.sound());
    }

    public static ResolvedMessage from(final ConfiguredMessage configured) {
        return from(configured, Collections.emptyMap());
    }

    public MessageKey key() {
        return key;
    }

    public ChatChannel chat() {
        return chat;
    }

    public ActionBarChannel actionBar() {
        return actionBar;
    }

    public TitleChannel title() {
        return title;
    }

    public SoundChannel sound() {
        return sound;
    }

    private static String applyReplacements(final String text,
                                             final Map<String, String> replacements) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String result = text;
        for (final Map.Entry<String, String> entry : replacements.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    private static List<String> applyReplacements(final List<String> lines,
                                                    final Map<String, String> replacements) {
        return lines.stream()
                .map(line -> applyReplacements(line, replacements))
                .collect(Collectors.toList());
    }
}
