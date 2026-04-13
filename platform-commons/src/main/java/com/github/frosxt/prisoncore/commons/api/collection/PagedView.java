package com.github.frosxt.prisoncore.commons.api.collection;

import java.util.List;

public interface PagedView<T> {
    List<T> page(int pageNumber);
    int totalPages();
    int totalItems();
    int pageSize();
    boolean hasNext(int currentPage);
    boolean hasPrevious(int currentPage);
}
