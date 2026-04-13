package com.github.frosxt.prisoncore.commons.core.validation;

import com.github.frosxt.prisoncore.commons.api.validation.ValidationPipeline;
import com.github.frosxt.prisoncore.commons.api.validation.ValidationResult;
import com.github.frosxt.prisoncore.commons.api.validation.ValidationRule;

import java.util.ArrayList;
import java.util.List;

public final class DefaultValidationPipeline<T> implements ValidationPipeline<T> {
    private final List<ValidationRule<T>> rules = new ArrayList<>();

    @Override
    public ValidationResult validate(final T value) {
        ValidationResult result = ValidationResult.success();
        for (final ValidationRule<T> rule : rules) {
            result = result.merge(rule.validate(value));
        }
        return result;
    }

    @Override
    public ValidationPipeline<T> addRule(final ValidationRule<T> rule) {
        rules.add(rule);
        return this;
    }
}
