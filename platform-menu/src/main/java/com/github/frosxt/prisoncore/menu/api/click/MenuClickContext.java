package com.github.frosxt.prisoncore.menu.api.click;

import java.util.UUID;

public final class MenuClickContext {
    private final UUID viewerId;
    private final int slot;
    private final ClickType clickType;

    public MenuClickContext(final UUID viewerId, final int slot, final ClickType clickType) {
        this.viewerId = viewerId;
        this.slot = slot;
        this.clickType = clickType;
    }

    public UUID viewerId() {
        return viewerId;
    }

    public int slot() {
        return slot;
    }

    public ClickType clickType() {
        return clickType;
    }

    public boolean isLeftClick() {
        return clickType == ClickType.LEFT || clickType == ClickType.SHIFT_LEFT;
    }

    public boolean isRightClick() {
        return clickType == ClickType.RIGHT || clickType == ClickType.SHIFT_RIGHT;
    }

    public boolean isShiftClick() {
        return clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT;
    }
}
