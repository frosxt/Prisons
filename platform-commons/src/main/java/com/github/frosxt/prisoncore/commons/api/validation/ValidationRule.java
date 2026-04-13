package com.github.frosxt.prisoncore.commons.api.validation;

@FunctionalInterface
public interface ValidationRule<T> {
    ValidationResult validate(T value);
}
