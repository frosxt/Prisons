package com.github.frosxt.prisoncore.config.api;

import java.util.Collections;
import java.util.List;

public final class ConfigValidationReport {
    private final List<String> errors;

    public ConfigValidationReport(final List<String> errors) {
        this.errors = Collections.unmodifiableList(errors);
    }

    public static ConfigValidationReport valid() {
        return new ConfigValidationReport(Collections.emptyList());
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<String> errors() {
        return errors;
    }
}
