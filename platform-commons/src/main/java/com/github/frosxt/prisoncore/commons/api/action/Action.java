package com.github.frosxt.prisoncore.commons.api.action;

@FunctionalInterface
public interface Action<T> {
    ActionResult<T> execute(T input);
}
