package com.github.frosxt.prisoncore.menu.api.layout;

import java.util.Collections;
import java.util.Map;

public final class MenuLayout {
    private final Map<Integer, SlotDescriptor> slots;

    public MenuLayout(final Map<Integer, SlotDescriptor> slots) {
        this.slots = Collections.unmodifiableMap(slots);
    }

    public Map<Integer, SlotDescriptor> slots() {
        return slots;
    }

    public SlotDescriptor slot(final int index) {
        return slots.get(index);
    }
}
