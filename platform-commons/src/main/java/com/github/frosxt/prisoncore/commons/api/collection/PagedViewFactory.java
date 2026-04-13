package com.github.frosxt.prisoncore.commons.api.collection;

import java.util.List;

public interface PagedViewFactory {
    <T> PagedView<T> create(List<T> items, int pageSize);
}
