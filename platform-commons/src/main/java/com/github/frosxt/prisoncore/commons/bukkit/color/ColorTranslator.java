package com.github.frosxt.prisoncore.commons.bukkit.color;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts ampersand color codes ({@code &a}, {@code &#rrggbb}) into the legacy
 * section-sign format Bukkit expects. Every player-facing string in the platform
 * and in modules is routed through here — never write {@code §} codes by hand.
 */
public final class ColorTranslator {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private ColorTranslator() {}

    /** @return the translated string with color codes applied. Null input returns empty. */
    public static String colorize(final String text) {
        if (text == null) {
            return "";
        }
        final Matcher matcher = HEX_PATTERN.matcher(text);
        final StringBuilder buffer = new StringBuilder();
        while (matcher.find()) {
            final String hex = matcher.group(1);
            final StringBuilder replacement = new StringBuilder("\u00a7x");
            for (final char c : hex.toCharArray()) {
                replacement.append("\u00a7").append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    public static String stripColor(final String text) {
        return ChatColor.stripColor(colorize(text));
    }
}
