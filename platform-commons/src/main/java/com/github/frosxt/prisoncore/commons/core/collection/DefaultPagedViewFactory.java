package com.github.frosxt.prisoncore.commons.core.collection;

import com.github.frosxt.prisoncore.commons.api.collection.PagedView;
import com.github.frosxt.prisoncore.commons.api.collection.PagedViewFactory;

import java.util.List;

public final class DefaultPagedViewFactory implements PagedViewFactory {
    @Override
    public <T> PagedView<T> create(final List<T> items, final int pageSize) {
        return new ArrayPagedView<>(items, pageSize);
    }
}
