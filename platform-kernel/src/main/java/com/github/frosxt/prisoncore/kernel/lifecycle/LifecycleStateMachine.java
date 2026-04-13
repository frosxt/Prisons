package com.github.frosxt.prisoncore.kernel.lifecycle;

import com.github.frosxt.prisoncore.api.lifecycle.LifecycleListener;
import com.github.frosxt.prisoncore.api.lifecycle.LifecycleState;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class LifecycleStateMachine {
    private volatile LifecycleState current = LifecycleState.CREATED;
    private final List<LifecycleListener> listeners = new CopyOnWriteArrayList<>();

    public LifecycleState current() {
        return current;
    }

    public synchronized void transition(final LifecycleState target) {
        final LifecycleState from = current;
        if (target.ordinal() <= from.ordinal() && target != LifecycleState.DISABLED) {
            throw new IllegalStateException("Invalid transition: " + from + " -> " + target);
        }
        current = target;
        for (final LifecycleListener listener : listeners) {
            listener.onStateChange(from, target);
        }
    }

    public void addListener(final LifecycleListener listener) {
        listeners.add(listener);
    }

    public void removeListener(final LifecycleListener listener) {
        listeners.remove(listener);
    }
}
