package com.github.frosxt.prisoncore.commons.api.action;

import java.util.Optional;

public sealed interface ActionResult<T> {
    boolean isSuccess();
    Optional<T> value();

    Optional<String> error();

    record Success<T>(T val) implements ActionResult<T> {
        @Override public boolean isSuccess() { return true; }
        @Override public Optional<T> value() { return Optional.ofNullable(val); }
        @Override public Optional<String> error() { return Optional.empty(); }
    }

    record Failure<T>(String message) implements ActionResult<T> {
        @Override public boolean isSuccess() { return false; }
        @Override public Optional<T> value() { return Optional.empty(); }
        @Override public Optional<String> error() { return Optional.of(message); }
    }
}
