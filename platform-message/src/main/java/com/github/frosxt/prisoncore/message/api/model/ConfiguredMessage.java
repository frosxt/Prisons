package com.github.frosxt.prisoncore.message.api.model;

import com.github.frosxt.prisoncore.message.api.MessageKey;
import com.github.frosxt.prisoncore.message.api.channel.ActionBarChannel;
import com.github.frosxt.prisoncore.message.api.channel.ChatChannel;
import com.github.frosxt.prisoncore.message.api.channel.SoundChannel;
import com.github.frosxt.prisoncore.message.api.channel.TitleChannel;

import java.util.Objects;

public final class ConfiguredMessage {

    private final MessageKey key;
    private final ChatChannel chat;
    private final ActionBarChannel actionBar;
    private final TitleChannel title;
    private final SoundChannel sound;

    public ConfiguredMessage(final MessageKey key, final ChatChannel chat,
                              final ActionBarChannel actionBar, final TitleChannel title,
                              final SoundChannel sound) {
        this.key = Objects.requireNonNull(key, "key");
        this.chat = chat != null ? chat : ChatChannel.disabled();
        this.actionBar = actionBar != null ? actionBar : ActionBarChannel.disabled();
        this.title = title != null ? title : TitleChannel.disabled();
        this.sound = sound != null ? sound : SoundChannel.disabled();
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
}
