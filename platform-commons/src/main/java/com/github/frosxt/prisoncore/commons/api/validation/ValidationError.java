package com.github.frosxt.prisoncore.commons.api.validation;

import java.util.Objects;

public final class ValidationError {
    private final String field;
    private final String message;

    public ValidationError(final String field, final String message) {
        this.field = Objects.requireNonNull(field);
        this.message = Objects.requireNonNull(message);
    }

    public String field() {
        return field;
    }

    public String message() {
        return message;
    }

    @Override
    public String toString() {
        return field + ": " + message;
    }
}
