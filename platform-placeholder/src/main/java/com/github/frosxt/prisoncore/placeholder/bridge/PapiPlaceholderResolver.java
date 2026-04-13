package com.github.frosxt.prisoncore.placeholder.bridge;

import com.github.frosxt.prisoncore.placeholder.api.PlaceholderContext;
import com.github.frosxt.prisoncore.placeholder.api.PlaceholderResolver;
import com.github.frosxt.prisoncore.placeholder.api.ResolutionResult;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public final class PapiPlaceholderResolver implements PlaceholderResolver {
    @Override
    public ResolutionResult resolve(final PlaceholderContext context, final String token) {
        final OfflinePlayer player = Bukkit.getOfflinePlayer(context.playerId());
        final String placeholder = "%" + token + "%";
        final String result = PlaceholderAPI.setPlaceholders(player, placeholder);

        if (!result.equals(placeholder)) {
            return new ResolutionResult.Resolved(result);
        }
        return new ResolutionResult.Unresolved(token);
    }
}
