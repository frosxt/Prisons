package com.github.frosxt.prisoncore.placeholder.api;

/**
 * Substitutes {@code %namespace_token%} placeholders in arbitrary templates.
 * Modules register a {@link PlaceholderResolver} against their namespace to
 * expose custom tokens; resolvers are keyed by namespace and only consulted
 * for tokens that start with that prefix.
 */
public interface PlaceholderService {

    /** Replace every {@code %namespace_token%} in the template with its resolved value. */
    String process(String template, PlaceholderContext context);

    /** Register a resolver for the given namespace. Throws on duplicate registration. */
    void registerResolver(String namespace, PlaceholderResolver resolver);

    void unregisterResolver(String namespace);
}
