package com.github.frosxt.prisoncore.commons.core.collection;

import com.github.frosxt.prisoncore.commons.api.collection.PagedView;

import java.util.Collections;
import java.util.List;

public final class ArrayPagedView<T> implements PagedView<T> {
    private final List<T> items;
    private final int pageSize;

    public ArrayPagedView(final List<T> items, final int pageSize) {
        this.items = Collections.unmodifiableList(items);
        this.pageSize = Math.max(1, pageSize);
    }

    @Override
    public List<T> page(final int pageNumber) {
        final int start = pageNumber * pageSize;
        if (start >= items.size()) {
            return Collections.emptyList();
        }
        final int end = Math.min(start + pageSize, items.size());
        return items.subList(start, end);
    }

    @Override
    public int totalPages() {
        return Math.max(1, (int) Math.ceil((double) items.size() / pageSize));
    }

    @Override
    public int totalItems() {
        return items.size();
    }

    @Override
    public int pageSize() {
        return pageSize;
    }

    @Override
    public boolean hasNext(final int currentPage) {
        return currentPage < totalPages() - 1;
    }

    @Override
    public boolean hasPrevious(final int currentPage) {
        return currentPage > 0;
    }
}
