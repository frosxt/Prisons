package com.github.frosxt.prisoncore.menu.api;

import java.util.UUID;

/**
 * Opens and manages player-facing inventory menus. Resolve from the service container
 * and call {@link #open(UUID, MenuDescriptor)} to show a menu to a specific player.
 */
public interface MenuService {

    /** Open a non-paginated menu for the player. Closes any menu currently open. */
    void open(UUID playerId, MenuDescriptor descriptor);

    /** Open a paginated menu at the given zero-based page. */
    void open(UUID playerId, PaginatedMenuDescriptor descriptor, int page);

    void close(UUID playerId);

    boolean hasOpenMenu(UUID playerId);

    /** Re-render the currently open menu without re-opening the inventory. */
    void refresh(UUID playerId);
}
