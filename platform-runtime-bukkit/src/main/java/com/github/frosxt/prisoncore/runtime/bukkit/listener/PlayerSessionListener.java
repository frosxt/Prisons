package com.github.frosxt.prisoncore.runtime.bukkit.listener;

import com.github.frosxt.prisoncore.kernel.container.session.SessionScopeManager;
import com.github.frosxt.prisoncore.player.api.PlayerProfile;
import com.github.frosxt.prisoncore.player.api.PlayerProfileService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class PlayerSessionListener implements Listener {
    private final PlayerProfileService profileService;
    private final SessionScopeManager sessionScopeManager;
    private final Plugin plugin;
    private final Logger logger;

    public PlayerSessionListener(final PlayerProfileService profileService, final SessionScopeManager sessionScopeManager, final Plugin plugin) {
        this.profileService = profileService;
        this.sessionScopeManager = sessionScopeManager;
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        profileService.loadOrCreate(player.getUniqueId(), player.getName())
                .thenAccept(profile ->
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            if (player.isOnline()) {
                                profileService.createSession(player.getUniqueId(), profile);
                                sessionScopeManager.create(player.getUniqueId());
                            }
                        }))
                .exceptionally(ex -> {
                    logger.log(Level.WARNING, "[PrisonCore] Failed to load profile for "
                            + player.getName() + ", creating new", ex);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (player.isOnline()) {
                            profileService.createSession(player.getUniqueId(), PlayerProfile.createNew(player.getUniqueId(), player.getName()));
                            sessionScopeManager.create(player.getUniqueId());
                        }
                    });
                    return null;
                });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(final PlayerQuitEvent event) {
        sessionScopeManager.destroy(event.getPlayer().getUniqueId());
        profileService.destroySession(event.getPlayer().getUniqueId());
    }
}
