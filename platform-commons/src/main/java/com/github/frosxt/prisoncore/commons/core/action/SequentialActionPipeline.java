package com.github.frosxt.prisoncore.commons.core.action;

import com.github.frosxt.prisoncore.commons.api.action.Action;
import com.github.frosxt.prisoncore.commons.api.action.ActionPipeline;
import com.github.frosxt.prisoncore.commons.api.action.ActionResult;

import java.util.ArrayList;
import java.util.List;

public final class SequentialActionPipeline<T> implements ActionPipeline<T> {
    private final List<Action<T>> actions = new ArrayList<>();

    @Override
    public ActionResult<T> execute(final T input) {
        T current = input;
        for (final Action<T> action : actions) {
            final ActionResult<T> result = action.execute(current);
            if (!result.isSuccess()) {
                return result;
            }
            current = result.value().orElse(current);
        }
        return new ActionResult.Success<>(current);
    }

    @Override
    public ActionPipeline<T> addAction(final Action<T> action) {
        actions.add(action);
        return this;
    }
}
