package com.github.frosxt.prisoncore.api.util;

import java.util.Optional;
import java.util.function.Function;

public sealed interface Result<T, E> {

    static <T, E> Result<T, E> ok(final T value) {
        return new Success<>(value);
    }

    static <T, E> Result<T, E> err(final E error) {
        return new Failure<>(error);
    }

    boolean isOk();
    boolean isErr();
    Optional<T> value();
    Optional<E> error();

    <U> Result<U, E> map(Function<T, U> mapper);
    <U> Result<U, E> flatMap(Function<T, Result<U, E>> mapper);

    record Success<T, E>(T val) implements Result<T, E> {
        @Override public boolean isOk() {
            return true;
        }

        @Override public boolean isErr() {
            return false;
        }

        @Override public Optional<T> value() {
            return Optional.of(val);
        }

        @Override public Optional<E> error() {
            return Optional.empty();
        }

        @Override public <U> Result<U, E> map(final Function<T, U> mapper) {
            return Result.ok(mapper.apply(val));
        }

        @Override public <U> Result<U, E> flatMap(final Function<T, Result<U, E>> mapper) {
            return mapper.apply(val);
        }
    }

    record Failure<T, E>(E err) implements Result<T, E> {
        @Override public boolean isOk() {
            return false;
        }

        @Override public boolean isErr() {
            return true; 
        }

        @Override public Optional<T> value() {
            return Optional.empty();
        }

        @Override public Optional<E> error() {
            return Optional.of(err);
        }

        @Override @SuppressWarnings("unchecked")
        public <U> Result<U, E> map(final Function<T, U> mapper) {
            return (Result<U, E>) this;
        }

        @Override @SuppressWarnings("unchecked")
        public <U> Result<U, E> flatMap(final Function<T, Result<U, E>> mapper) {
            return (Result<U, E>) this;
        }
    }
}
