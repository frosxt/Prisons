package com.github.frosxt.prisoncore.menu.bukkit;

import com.github.frosxt.prisoncore.menu.api.click.ClickType;
import com.github.frosxt.prisoncore.menu.api.click.MenuClickContext;
import com.github.frosxt.prisoncore.menu.api.layout.MenuLayout;
import com.github.frosxt.prisoncore.menu.api.layout.SlotDescriptor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public final class BukkitMenuListener implements Listener {
    private final BukkitMenuService menuService;

    public BukkitMenuListener(final BukkitMenuService menuService) {
        this.menuService = menuService;
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof final Player player)) {
            return;
        }

        final BukkitMenuService.ActiveMenu active = menuService.getActiveMenu(player.getUniqueId());
        if (active == null) {
            return;
        }
        if (!event.getInventory().equals(active.inventory())) {
            return;
        }

        event.setCancelled(true);

        final int slot = event.getRawSlot();
        if (slot < 0 || slot >= active.descriptor().size()) {
            return;
        }

        final MenuLayout layout = active.descriptor().layout();
        if (layout == null) {
            return;
        }

        final SlotDescriptor slotDesc = layout.slot(slot);
        if (slotDesc == null || slotDesc.handler() == null) {
            return;
        }

        final ClickType clickType = mapClickType(event.getClick());
        final MenuClickContext context = new MenuClickContext(player.getUniqueId(), slot, clickType);
        slotDesc.handler().handle(context);
    }

    @EventHandler
    public void onInventoryDrag(final InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof final Player player)) {
            return;
        }

        final BukkitMenuService.ActiveMenu active = menuService.getActiveMenu(player.getUniqueId());
        if (active == null) {
            return;
        }
        if (!event.getInventory().equals(active.inventory())) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof final Player player)) {
            return;
        }
        final BukkitMenuService.ActiveMenu active = menuService.getActiveMenu(player.getUniqueId());
        if (active != null && event.getInventory().equals(active.inventory())) {
            menuService.removeActiveMenu(player.getUniqueId());
        }
    }

    private ClickType mapClickType(final org.bukkit.event.inventory.ClickType bukkit) {
        return switch (bukkit) {
            case LEFT -> ClickType.LEFT;
            case RIGHT -> ClickType.RIGHT;
            case SHIFT_LEFT -> ClickType.SHIFT_LEFT;
            case SHIFT_RIGHT -> ClickType.SHIFT_RIGHT;
            case MIDDLE -> ClickType.MIDDLE;
            case DROP, CONTROL_DROP -> ClickType.DROP;
            case DOUBLE_CLICK -> ClickType.DOUBLE_CLICK;
            default -> ClickType.LEFT;
        };
    }
}
