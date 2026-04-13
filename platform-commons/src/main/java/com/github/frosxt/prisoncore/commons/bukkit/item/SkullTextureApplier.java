package com.github.frosxt.prisoncore.commons.bukkit.item;

import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;

public final class SkullTextureApplier {
    private static final String TEXTURES_URL_PREFIX = "https://textures.minecraft.net/texture/";

    private SkullTextureApplier() {}

    public static void applyTexture(final SkullMeta meta, final String base64Texture) {
        final String textureUrl = resolveTextureUrl(base64Texture);
        final PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
        final PlayerTextures textures = profile.getTextures();
        try {
            textures.setSkin(new URL(textureUrl));
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException("Invalid skull texture: " + base64Texture, e);
        }
        profile.setTextures(textures);
        meta.setOwnerProfile(profile);
    }

    public static void applyOwner(final SkullMeta meta, final String playerName) {
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(playerName));
    }

    public static void applyOwner(final SkullMeta meta, final UUID uuid) {
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
    }

    private static String resolveTextureUrl(final String input) {
        if (input.startsWith(TEXTURES_URL_PREFIX)) {
            return input;
        }

        try {
            final String decoded = new String(Base64.getDecoder().decode(input));
            final int urlStart = decoded.indexOf(TEXTURES_URL_PREFIX);
            if (urlStart != -1) {
                final int urlEnd = decoded.indexOf('"', urlStart);
                if (urlEnd != -1) {
                    return decoded.substring(urlStart, urlEnd);
                }
            }
        } catch (final IllegalArgumentException ignored) {
            // Not valid base64
        }

        return TEXTURES_URL_PREFIX + input;
    }
}
