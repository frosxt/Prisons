package com.github.frosxt.prisoncore.menu.api.layout;

import com.github.frosxt.prisoncore.menu.api.click.ClickHandler;

/**
 * Describes a single menu slot: how to render it and what happens on click.
 * Use {@link #displayOnly(ItemProvider)} for passive slots (borders, info items).
 */
public final class SlotDescriptor {
    private final ItemProvider itemProvider;
    private final ClickHandler handler;

    public SlotDescriptor(final ItemProvider itemProvider, final ClickHandler handler) {
        this.itemProvider = itemProvider;
        this.handler = handler;
    }

    public ItemProvider itemProvider() {
        return itemProvider;
    }

    public ClickHandler handler() {
        return handler;
    }

    /**
     * Provides the item to render in this slot. Decoupled from Bukkit ItemStack
     * so the API layer stays platform-independent. The Bukkit renderer casts
     * the returned object to ItemStack.
     */
    @FunctionalInterface
    public interface ItemProvider {
        Object createItem();
    }

    public static SlotDescriptor of(final ItemProvider item, final ClickHandler handler) {
        return new SlotDescriptor(item, handler);
    }

    public static SlotDescriptor displayOnly(final ItemProvider item) {
        return new SlotDescriptor(item, null);
    }
}
