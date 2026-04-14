package com.github.frosxt.prisoncore.menu.api;

import com.github.frosxt.prisoncore.menu.api.layout.MenuLayout;
import com.github.frosxt.prisoncore.menu.api.layout.SlotDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Immutable description of a paginated inventory menu. Items are laid out into a
 * contiguous content range; previous/next buttons are placed at configurable slots
 * outside the range, defaulting to the bottom-left and bottom-right corners.
 */
public final class PaginatedMenuDescriptor {

    private static final Logger LOGGER = Logger.getLogger(PaginatedMenuDescriptor.class.getName());
    private static final int UNSET_SLOT = -1;

    private final String id;
    private final String title;
    private final int rows;
    private final int contentStartSlot;
    private final int contentEndSlot;
    private final SlotDescriptor previousButton;
    private final SlotDescriptor nextButton;
    private final int previousButtonSlot;
    private final int nextButtonSlot;
    private final List<SlotDescriptor> items;

    private PaginatedMenuDescriptor(final Builder builder) {
        this.id = Objects.requireNonNull(builder.id);
        this.title = Objects.requireNonNull(builder.title);
        this.rows = builder.rows;
        this.contentStartSlot = builder.contentStartSlot;
        this.contentEndSlot = builder.contentEndSlot;
        this.previousButton = builder.previousButton;
        this.nextButton = builder.nextButton;
        this.previousButtonSlot = builder.previousButtonSlot != UNSET_SLOT
                ? builder.previousButtonSlot
                : rows * 9 - 9;
        this.nextButtonSlot = builder.nextButtonSlot != UNSET_SLOT
                ? builder.nextButtonSlot
                : rows * 9 - 1;
        this.items = Collections.unmodifiableList(new ArrayList<>(builder.items));
        validateButtonPlacement();
    }

    public String id() {
        return id;
    }

    public String title() {
        return title;
    }

    public int rows() {
        return rows;
    }

    public int contentStartSlot() {
        return contentStartSlot;
    }

    public int contentEndSlot() {
        return contentEndSlot;
    }

    public SlotDescriptor previousButton() {
        return previousButton;
    }

    public SlotDescriptor nextButton() {
        return nextButton;
    }

    public int previousButtonSlot() {
        return previousButtonSlot;
    }

    public int nextButtonSlot() {
        return nextButtonSlot;
    }

    public List<SlotDescriptor> items() {
        return items;
    }

    public int totalPages() {
        final int contentSlotsPerPage = contentEndSlot - contentStartSlot + 1;
        if (contentSlotsPerPage <= 0 || items.isEmpty()) {
            return 1;
        }
        return (int) Math.ceil((double) items.size() / contentSlotsPerPage);
    }

    public MenuDescriptor buildPage(final int pageNumber) {
        final int contentSlotsPerPage = contentEndSlot - contentStartSlot + 1;
        final int offset = pageNumber * contentSlotsPerPage;
        final Map<Integer, SlotDescriptor> slots = new HashMap<>();

        for (int i = 0; i < contentSlotsPerPage; i++) {
            final int itemIndex = offset + i;
            if (itemIndex >= items.size()) {
                break;
            }
            slots.put(contentStartSlot + i, items.get(itemIndex));
        }

        if (pageNumber > 0 && previousButton != null) {
            slots.put(previousButtonSlot, previousButton);
        }

        if (pageNumber < totalPages() - 1 && nextButton != null) {
            slots.put(nextButtonSlot, nextButton);
        }

        final MenuLayout layout = new MenuLayout(slots);
        return new MenuDescriptor(id, title, rows, layout);
    }

    private void validateButtonPlacement() {
        final int inventorySize = rows * 9;
        if (previousButton != null) {
            checkSlotBounds("previousButton", previousButtonSlot, inventorySize);
            checkSlotCollision("previousButton", previousButtonSlot);
        }
        if (nextButton != null) {
            checkSlotBounds("nextButton", nextButtonSlot, inventorySize);
            checkSlotCollision("nextButton", nextButtonSlot);
        }
        if (previousButton != null && nextButton != null && previousButtonSlot == nextButtonSlot) {
            LOGGER.warning("[PrisonCore] PaginatedMenuDescriptor '" + id
                    + "' has previousButton and nextButton at the same slot " + previousButtonSlot
                    + "; the next button will overwrite the previous button on every page after the first.");
        }
    }

    private void checkSlotBounds(final String label, final int slot, final int inventorySize) {
        if (slot < 0 || slot >= inventorySize) {
            LOGGER.warning("[PrisonCore] PaginatedMenuDescriptor '" + id + "' " + label
                    + " slot " + slot + " is outside the inventory (" + inventorySize + " slots)."
                    + " The button will not be rendered.");
        }
    }

    private void checkSlotCollision(final String label, final int slot) {
        if (slot >= contentStartSlot && slot <= contentEndSlot) {
            LOGGER.warning("[PrisonCore] PaginatedMenuDescriptor '" + id + "' " + label
                    + " slot " + slot + " lands inside the content range ["
                    + contentStartSlot + ".." + contentEndSlot + "];"
                    + " it will overwrite a content item on pages that fill that slot.");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String title;
        private int rows;
        private int contentStartSlot;
        private int contentEndSlot;
        private SlotDescriptor previousButton;
        private SlotDescriptor nextButton;
        private int previousButtonSlot = UNSET_SLOT;
        private int nextButtonSlot = UNSET_SLOT;
        private final List<SlotDescriptor> items = new ArrayList<>();

        private Builder() {}

        public Builder id(final String id) {
            this.id = id;
            return this;
        }

        public Builder title(final String title) {
            this.title = title;
            return this;
        }

        public Builder rows(final int rows) {
            this.rows = rows;
            return this;
        }

        public Builder contentRange(final int start, final int end) {
            this.contentStartSlot = start;
            this.contentEndSlot = end;
            return this;
        }

        public Builder previousButton(final SlotDescriptor previousButton) {
            this.previousButton = previousButton;
            return this;
        }

        public Builder nextButton(final SlotDescriptor nextButton) {
            this.nextButton = nextButton;
            return this;
        }

        /**
         * Override the inventory slot where the previous-page button renders.
         * Defaults to the bottom-left slot ({@code rows * 9 - 9}).
         */
        public Builder previousButtonSlot(final int slot) {
            this.previousButtonSlot = slot;
            return this;
        }

        /**
         * Override the inventory slot where the next-page button renders.
         * Defaults to the bottom-right slot ({@code rows * 9 - 1}).
         */
        public Builder nextButtonSlot(final int slot) {
            this.nextButtonSlot = slot;
            return this;
        }

        public Builder items(final List<SlotDescriptor> items) {
            this.items.clear();
            this.items.addAll(items);
            return this;
        }

        public Builder addItem(final SlotDescriptor item) {
            this.items.add(item);
            return this;
        }

        public PaginatedMenuDescriptor build() {
            return new PaginatedMenuDescriptor(this);
        }
    }
}
