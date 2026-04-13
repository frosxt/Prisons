package com.github.frosxt.prisoncore.commons.api.validation;

import java.util.Collections;
import java.util.List;

public final class ValidationResult {
    private static final ValidationResult SUCCESS = new ValidationResult(Collections.emptyList());

    private final List<ValidationError> errors;

    private ValidationResult(final List<ValidationError> errors) {
        this.errors = Collections.unmodifiableList(errors);
    }

    public static ValidationResult success() {
        return SUCCESS;
    }

    public static ValidationResult failure(final List<ValidationError> errors) {
        return new ValidationResult(errors);
    }

    public static ValidationResult failure(final String field, final String message) {
        return new ValidationResult(List.of(new ValidationError(field, message)));
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<ValidationError> errors() {
        return errors;
    }

    public ValidationResult merge(final ValidationResult other) {
        if (this.isValid()) {
            return other;
        }
        if (other.isValid()) {
            return this;
        }
        final var combined = new java.util.ArrayList<>(this.errors);
        combined.addAll(other.errors);
        return new ValidationResult(combined);
    }
}
