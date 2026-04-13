package com.github.frosxt.prisoncore.menu.api;

import com.github.frosxt.prisoncore.menu.api.layout.MenuLayout;
import com.github.frosxt.prisoncore.menu.api.layout.SlotDescriptor;

import java.util.*;

public final class PaginatedMenuDescriptor {
    private final String id;
    private final String title;
    private final int rows;
    private final int contentStartSlot;
    private final int contentEndSlot;
    private final SlotDescriptor previousButton;
    private final SlotDescriptor nextButton;
    private final List<SlotDescriptor> items;

    private PaginatedMenuDescriptor(final Builder builder) {
        this.id = Objects.requireNonNull(builder.id);
        this.title = Objects.requireNonNull(builder.title);
        this.rows = builder.rows;
        this.contentStartSlot = builder.contentStartSlot;
        this.contentEndSlot = builder.contentEndSlot;
        this.previousButton = builder.previousButton;
        this.nextButton = builder.nextButton;
        this.items = Collections.unmodifiableList(new ArrayList<>(builder.items));
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
            final int prevSlot = rows * 9 - 9;
            slots.put(prevSlot, previousButton);
        }

        if (pageNumber < totalPages() - 1 && nextButton != null) {
            final int nextSlot = rows * 9 - 1;
            slots.put(nextSlot, nextButton);
        }

        final MenuLayout layout = new MenuLayout(slots);
        return new MenuDescriptor(id, title, rows, layout);
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
