package com.github.frosxt.prisoncore.commons.core.validation;

import com.github.frosxt.prisoncore.commons.api.validation.ValidationResult;
import com.github.frosxt.prisoncore.commons.api.validation.ValidationRule;

import java.util.regex.Pattern;

public final class CommonRules {
    private CommonRules() {
        throw new UnsupportedOperationException("Utility classes cannot be instantiated");
    }

    public static <T> ValidationRule<T> notNull(final String field) {
        return value -> value == null
                ? ValidationResult.failure(field, "must not be null")
                : ValidationResult.success();
    }

    public static ValidationRule<String> notEmpty(final String field) {
        return value -> (value == null || value.isEmpty())
                ? ValidationResult.failure(field, "must not be empty")
                : ValidationResult.success();
    }

    public static ValidationRule<String> notBlank(final String field) {
        return value -> (value == null || value.isBlank())
                ? ValidationResult.failure(field, "must not be blank")
                : ValidationResult.success();
    }

    public static ValidationRule<String> minLength(final String field, final int min) {
        return value -> (value != null && value.length() < min)
                ? ValidationResult.failure(field, "must be at least " + min + " characters")
                : ValidationResult.success();
    }

    public static ValidationRule<String> maxLength(final String field, final int max) {
        return value -> (value != null && value.length() > max)
                ? ValidationResult.failure(field, "must be at most " + max + " characters")
                : ValidationResult.success();
    }

    public static ValidationRule<String> matches(final String field, final Pattern pattern) {
        return value -> (value != null && !pattern.matcher(value).matches())
                ? ValidationResult.failure(field, "must match pattern: " + pattern.pattern())
                : ValidationResult.success();
    }

    public static <N extends Number & Comparable<N>> ValidationRule<N> min(final String field, final N min) {
        return value -> (value != null && value.compareTo(min) < 0)
                ? ValidationResult.failure(field, "must be at least " + min)
                : ValidationResult.success();
    }

    public static <N extends Number & Comparable<N>> ValidationRule<N> max(final String field, final N max) {
        return value -> (value != null && value.compareTo(max) > 0)
                ? ValidationResult.failure(field, "must be at most " + max)
                : ValidationResult.success();
    }

    public static <N extends Number & Comparable<N>> ValidationRule<N> range(final String field, final N min, final N max) {
        return value -> {
            if (value == null) {
                return ValidationResult.success();
            }
            if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
                return ValidationResult.failure(field, "must be between " + min + " and " + max);
            }
            return ValidationResult.success();
        };
    }
}
