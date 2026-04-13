package com.github.frosxt.prisoncore.menu.api;

import java.util.HashMap;
import java.util.Map;

public final class MenuSession {
    private int currentPage;
    private final Map<String, Object> state;

    public MenuSession() {
        this.currentPage = 0;
        this.state = new HashMap<>();
    }

    public int page() {
        return currentPage;
    }

    public void setPage(final int page) {
        this.currentPage = page;
    }

    public void setState(final String key, final Object value) {
        state.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T state(final String key, final Class<T> type) {
        final Object value = state.get(key);
        if (value == null) {
            return null;
        }
        return type.cast(value);
    }

    public void clear() {
        this.currentPage = 0;
        this.state.clear();
    }
}
