package com.github.frosxt.prisoncore.menu.api.layout;

import java.util.List;

public interface PagedDataSource<T> {
    List<T> items(int page, int pageSize);
    int totalItems();
}
