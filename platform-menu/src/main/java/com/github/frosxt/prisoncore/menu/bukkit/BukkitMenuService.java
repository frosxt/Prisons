package com.github.frosxt.prisoncore.menu.bukkit;

import com.github.frosxt.prisoncore.menu.api.MenuDescriptor;
import com.github.frosxt.prisoncore.menu.api.MenuService;
import com.github.frosxt.prisoncore.menu.api.MenuSession;
import com.github.frosxt.prisoncore.menu.api.PaginatedMenuDescriptor;
import com.github.frosxt.prisoncore.menu.api.layout.SlotDescriptor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BukkitMenuService implements MenuService {
    private final Map<UUID, ActiveMenu> activeMenus = new ConcurrentHashMap<>();
    private final Map<UUID, MenuSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void open(final UUID playerId, final MenuDescriptor descriptor) {
        final Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            return;
        }

        final Inventory inventory = Bukkit.createInventory(null, descriptor.size(), descriptor.title());

        if (descriptor.layout() != null) {
            for (final Map.Entry<Integer, SlotDescriptor> entry : descriptor.layout().slots().entrySet()) {
                final int slot = entry.getKey();
                final SlotDescriptor slotDesc = entry.getValue();
                if (slotDesc.itemProvider() != null) {
                    final Object item = slotDesc.itemProvider().createItem();
                    if (item instanceof final ItemStack itemStack) {
                        inventory.setItem(slot, itemStack);
                    }
                }
            }
        }

        activeMenus.put(playerId, new ActiveMenu(descriptor, inventory));
        player.openInventory(inventory);
    }

    @Override
    public void open(final UUID playerId, final PaginatedMenuDescriptor descriptor, final int page) {
        final int clampedPage = Math.max(0, Math.min(page, descriptor.totalPages() - 1));
        final MenuSession session = sessions.computeIfAbsent(playerId, k -> new MenuSession());
        session.setPage(clampedPage);
        final MenuDescriptor pageDescriptor = descriptor.buildPage(clampedPage);
        open(playerId, pageDescriptor);
    }

    @Override
    public void close(final UUID playerId) {
        activeMenus.remove(playerId);
        final Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            player.closeInventory();
        }
    }

    @Override
    public boolean hasOpenMenu(final UUID playerId) {
        return activeMenus.containsKey(playerId);
    }

    @Override
    public void refresh(final UUID playerId) {
        final ActiveMenu active = activeMenus.get(playerId);
        if (active == null) {
            return;
        }

        final Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            return;
        }

        final MenuDescriptor descriptor = active.descriptor();
        final Inventory inventory = active.inventory();

        if (descriptor.layout() != null) {
            for (final Map.Entry<Integer, SlotDescriptor> entry : descriptor.layout().slots().entrySet()) {
                final int slot = entry.getKey();
                final SlotDescriptor slotDesc = entry.getValue();
                if (slotDesc.itemProvider() != null) {
                    final Object item = slotDesc.itemProvider().createItem();
                    if (item instanceof final ItemStack itemStack) {
                        inventory.setItem(slot, itemStack);
                    }
                } else {
                    inventory.setItem(slot, null);
                }
            }
        }

        player.updateInventory();
    }

    public ActiveMenu getActiveMenu(final UUID playerId) {
        return activeMenus.get(playerId);
    }

    public void removeActiveMenu(final UUID playerId) {
        activeMenus.remove(playerId);
        sessions.remove(playerId);
    }

    public Optional<MenuSession> session(final UUID playerId) {
        return Optional.ofNullable(sessions.get(playerId));
    }

    public record ActiveMenu(MenuDescriptor descriptor, Inventory inventory) {}
}
