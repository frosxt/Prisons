package com.github.frosxt.prisoncore.placeholder.api;

/**
 * Resolves a single placeholder token (the part after the namespace prefix).
 * For {@code %economy_money_balance%}, a resolver registered under the
 * {@code economy} namespace receives the token {@code money_balance}.
 */
@FunctionalInterface
public interface PlaceholderResolver {

    /** @return a {@link ResolutionResult} — either a resolved value or {@code Unresolved}. */
    ResolutionResult resolve(PlaceholderContext context, String token);
}
