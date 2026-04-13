package com.github.frosxt.prisoncore.api.platform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public enum ThreadContext {
    MAIN_THREAD,
    ASYNC_SAFE,
    MIXED;

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface ThreadSafety {
        ThreadContext value();
    }
}
