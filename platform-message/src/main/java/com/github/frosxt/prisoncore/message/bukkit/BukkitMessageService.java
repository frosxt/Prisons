package com.github.frosxt.prisoncore.message.bukkit;

import com.github.frosxt.prisoncore.commons.bukkit.color.ColorTranslator;
import com.github.frosxt.prisoncore.message.api.MessageCatalog;
import com.github.frosxt.prisoncore.message.api.MessageKey;
import com.github.frosxt.prisoncore.message.api.MessageService;
import com.github.frosxt.prisoncore.message.api.channel.ActionBarChannel;
import com.github.frosxt.prisoncore.message.api.channel.ChatChannel;
import com.github.frosxt.prisoncore.message.api.channel.SoundChannel;
import com.github.frosxt.prisoncore.message.api.channel.TitleChannel;
import com.github.frosxt.prisoncore.message.api.model.ResolvedMessage;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public final class BukkitMessageService implements MessageService {

    private final MessageCatalog catalog;

    public BukkitMessageService(final MessageCatalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public void send(final Object sender, final MessageKey key) {
        send(sender, key, Collections.emptyMap());
    }

    @Override
    public void send(final Object sender, final MessageKey key, final Map<String, String> replacements) {
        final ResolvedMessage msg = catalog.resolve(key, replacements);
        if (sender instanceof final Player player) {
            deliverToPlayer(player, msg);
        } else if (sender instanceof final CommandSender cs) {
            deliverToConsole(cs, msg);
        }
    }

    @Override
    public void send(final UUID playerId, final MessageKey key) {
        send(playerId, key, Collections.emptyMap());
    }

    @Override
    public void send(final UUID playerId, final MessageKey key, final Map<String, String> replacements) {
        final Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            return;
        }
        deliverToPlayer(player, catalog.resolve(key, replacements));
    }

    @Override
    public void send(final UUID playerId, final String rawChat) {
        final Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            player.sendMessage(ColorTranslator.colorize(rawChat));
        }
    }

    @Override
    public void broadcast(final MessageKey key) {
        broadcast(key, Collections.emptyMap());
    }

    @Override
    public void broadcast(final MessageKey key, final Map<String, String> replacements) {
        final ResolvedMessage msg = catalog.resolve(key, replacements);
        for (final Player player : Bukkit.getOnlinePlayers()) {
            deliverToPlayer(player, msg);
        }
        deliverToConsole(Bukkit.getConsoleSender(), msg);
    }

    @Override
    public MessageCatalog catalog() {
        return catalog;
    }

    private void deliverToPlayer(final Player player, final ResolvedMessage msg) {
        final ChatChannel chat = msg.chat();
        if (chat.enabled()) {
            for (final String line : chat.lines()) {
                player.sendMessage(ColorTranslator.colorize(line));
            }
        }

        final ActionBarChannel actionBar = msg.actionBar();
        if (actionBar.enabled() && !actionBar.value().isEmpty()) {
            player.spigot().sendMessage(
                    ChatMessageType.ACTION_BAR,
                    new TextComponent(ColorTranslator.colorize(actionBar.value()))
            );
        }

        final TitleChannel title = msg.title();
        if (title.enabled()) {
            player.sendTitle(
                    ColorTranslator.colorize(title.title()),
                    ColorTranslator.colorize(title.subtitle()),
                    title.fadeIn(),
                    title.stay(),
                    title.fadeOut()
            );
        }

        final SoundChannel sound = msg.sound();
        if (sound.enabled() && !sound.value().isEmpty()) {
            try {
                final Sound bukkitSound = Sound.valueOf(sound.value().toUpperCase().replace('.', '_'));
                player.playSound(player.getLocation(), bukkitSound, sound.volume(), sound.pitch());
            } catch (final IllegalArgumentException e) {
                player.playSound(player.getLocation(), sound.value(), sound.volume(), sound.pitch());
            }
        }
    }

    private void deliverToConsole(final CommandSender sender, final ResolvedMessage msg) {
        final ChatChannel chat = msg.chat();
        if (chat.enabled()) {
            for (final String line : chat.lines()) {
                sender.sendMessage(ColorTranslator.colorize(line));
            }
        }
    }
}
