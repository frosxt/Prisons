package com.github.frosxt.prisoncore.menu.api.click;

import java.util.Map;

/**
 * Reaction to a player clicking a menu slot. Compose with the static helpers
 * ({@link #leftClick}, {@link #rightClick}, {@link #of(Map)}) to dispatch by click type.
 */
@FunctionalInterface
public interface ClickHandler {
    void handle(MenuClickContext context);

    static ClickHandler closing(final Runnable action) {
        return context -> action.run();
    }

    static ClickHandler of(final Map<ClickType, ClickHandler> handlers) {
        return context -> {
            final ClickHandler handler = handlers.get(context.clickType());
            if (handler != null) {
                handler.handle(context);
            }
        };
    }

    static ClickHandler leftClick(final ClickHandler handler) {
        return context -> {
            if (context.isLeftClick()) {
                handler.handle(context);
            }
        };
    }

    static ClickHandler rightClick(final ClickHandler handler) {
        return context -> {
            if (context.isRightClick()) {
                handler.handle(context);
            }
        };
    }

    static ClickHandler empty() {
        return context -> {};
    }
}
