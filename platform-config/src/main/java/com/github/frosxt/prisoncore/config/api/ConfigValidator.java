package com.github.frosxt.prisoncore.config.api;

public interface ConfigValidator<T> {
    ConfigValidationReport validate(T config);
}
