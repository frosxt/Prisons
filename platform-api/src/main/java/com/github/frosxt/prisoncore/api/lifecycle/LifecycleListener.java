package com.github.frosxt.prisoncore.api.lifecycle;

public interface LifecycleListener {
    void onStateChange(LifecycleState from, LifecycleState to);
}
