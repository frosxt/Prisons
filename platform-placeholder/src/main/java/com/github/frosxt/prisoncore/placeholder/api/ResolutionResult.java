package com.github.frosxt.prisoncore.placeholder.api;

/**
 * Outcome of a {@link PlaceholderResolver} call. Return {@link Unresolved}
 * to leave the placeholder untouched in the output.
 */
public sealed interface ResolutionResult {
    record Resolved(String value) implements ResolutionResult {
    }
    record Unresolved(String token) implements ResolutionResult {
    }
}
