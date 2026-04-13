package com.github.frosxt.prisoncore.commons.api.action;

public interface ActionPipeline<T> {
    ActionResult<T> execute(T input);

    ActionPipeline<T> addAction(Action<T> action);
}
