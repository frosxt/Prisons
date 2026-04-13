package com.github.frosxt.prisoncore.menu.api;

import com.github.frosxt.prisoncore.menu.api.layout.MenuLayout;
import com.github.frosxt.prisoncore.menu.api.layout.MenuLayoutPattern;
import com.github.frosxt.prisoncore.menu.api.layout.SlotDescriptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable description of an inventory menu: id, title, row count, and layout.
 * Use the fluent {@link Builder} (via {@code MenuDescriptor.builder(id, title, rows)})
 * to assemble slots, borders, and layouts without touching Bukkit types.
 */
public final class MenuDescriptor {
    private final String id;
    private final String title;
    private final int rows;
    private final MenuLayout layout;

    public MenuDescriptor(final String id, final String title, final int rows, final MenuLayout layout) {
        this.id = Objects.requireNonNull(id);
        this.title = Objects.requireNonNull(title);
        this.rows = rows;
        this.layout = layout;
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

    public int size() {
        return rows * 9;
    }

    public MenuLayout layout() {
        return layout;
    }

    public static Builder builder(final String id, final String title, final int rows) {
        return new Builder(id, title, rows);
    }

    public static final class Builder {
        private final String id;
        private final String title;
        private final int rows;
        private final Map<Integer, SlotDescriptor> slots = new HashMap<>();

        private Builder(final String id, final String title, final int rows) {
            this.id = Objects.requireNonNull(id);
            this.title = Objects.requireNonNull(title);
            this.rows = rows;
        }

        public Builder slot(final int index, final SlotDescriptor descriptor) {
            slots.put(index, descriptor);
            return this;
        }

        public Builder fill(final SlotDescriptor descriptor) {
            final int size = rows * 9;
            for (int i = 0; i < size; i++) {
                slots.putIfAbsent(i, descriptor);
            }
            return this;
        }

        public Builder border(final SlotDescriptor descriptor) {
            final int columns = 9;
            final int size = rows * columns;
            for (int i = 0; i < size; i++) {
                final int row = i / columns;
                final int col = i % columns;
                if (row == 0 || row == rows - 1 || col == 0 || col == columns - 1) {
                    slots.putIfAbsent(i, descriptor);
                }
            }
            return this;
        }

        public Builder row(final int row, final SlotDescriptor descriptor) {
            final int start = row * 9;
            for (int col = 0; col < 9; col++) {
                slots.putIfAbsent(start + col, descriptor);
            }
            return this;
        }

        public Builder column(final int col, final SlotDescriptor descriptor) {
            for (int row = 0; row < rows; row++) {
                slots.putIfAbsent(row * 9 + col, descriptor);
            }
            return this;
        }

        public Builder applyPattern(final MenuLayoutPattern pattern) {
            final MenuLayout patternLayout = pattern.apply(rows);
            for (final Map.Entry<Integer, SlotDescriptor> entry : patternLayout.slots().entrySet()) {
                slots.putIfAbsent(entry.getKey(), entry.getValue());
            }
            return this;
        }

        public MenuDescriptor build() {
            return new MenuDescriptor(id, title, rows, new MenuLayout(slots));
        }
    }
}
