package com.github.frosxt.prisoncore.commons.api.validation;

public interface ValidationPipeline<T> {
    ValidationResult validate(T value);
    ValidationPipeline<T> addRule(ValidationRule<T> rule);
}
