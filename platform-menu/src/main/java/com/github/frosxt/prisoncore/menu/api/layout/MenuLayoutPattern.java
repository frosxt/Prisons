package com.github.frosxt.prisoncore.menu.api.layout;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public final class MenuLayoutPattern {
    private final BiFunction<Integer, Integer, Optional<SlotDescriptor>> mapper;

    private MenuLayoutPattern(final BiFunction<Integer, Integer, Optional<SlotDescriptor>> mapper) {
        this.mapper = mapper;
    }

    public static MenuLayoutPattern border(final SlotDescriptor fill) {
        return new MenuLayoutPattern((slot, size) -> {
            final int columns = 9;
            final int rows = size / columns;
            final int row = slot / columns;
            final int col = slot % columns;
            if (row == 0 || row == rows - 1 || col == 0 || col == columns - 1) {
                return Optional.of(fill);
            }
            return Optional.empty();
        });
    }

    public static MenuLayoutPattern checkered(final SlotDescriptor a, final SlotDescriptor b) {
        return new MenuLayoutPattern((slot, size) -> {
            final int row = slot / 9;
            final int col = slot % 9;
            final boolean even = (row + col) % 2 == 0;
            return Optional.of(even ? a : b);
        });
    }

    public static MenuLayoutPattern corners(final SlotDescriptor corner) {
        return new MenuLayoutPattern((slot, size) -> {
            final int columns = 9;
            final int lastRow = (size / columns) - 1;
            if (slot == 0 || slot == columns - 1 || slot == lastRow * columns || slot == lastRow * columns + columns - 1) {
                return Optional.of(corner);
            }
            return Optional.empty();
        });
    }

    public static MenuLayoutPattern cross(final SlotDescriptor fill) {
        return new MenuLayoutPattern((slot, size) -> {
            final int columns = 9;
            final int rows = size / columns;
            final int row = slot / columns;
            final int col = slot % columns;
            final int centerRow = rows / 2;
            final int centerCol = columns / 2;
            if (row == centerRow || col == centerCol) {
                return Optional.of(fill);
            }
            return Optional.empty();
        });
    }

    public static MenuLayoutPattern alternatingRows(final SlotDescriptor a, final SlotDescriptor b) {
        return new MenuLayoutPattern((slot, size) -> {
            final int row = slot / 9;
            return Optional.of(row % 2 == 0 ? a : b);
        });
    }

    public MenuLayout apply(final int rows) {
        final int totalSize = rows * 9;
        final Map<Integer, SlotDescriptor> slots = new HashMap<>();
        for (int i = 0; i < totalSize; i++) {
            final Optional<SlotDescriptor> descriptor = mapper.apply(i, totalSize);
            if (descriptor.isPresent()) {
                slots.put(i, descriptor.get());
            }
        }
        return new MenuLayout(slots);
    }
}
