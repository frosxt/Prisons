package com.github.frosxt.prisoncore.message.api;

import com.github.frosxt.prisoncore.message.api.model.ConfiguredMessage;
import com.github.frosxt.prisoncore.message.api.model.ResolvedMessage;

import java.util.Map;

/**
 * Source of truth for every keyed message the platform knows about.
 * Backed by code-owned defaults ({@code MessageDefinitionRegistry}) merged with
 * user overrides from {@code messages.yml}.
 */
public interface MessageCatalog {

    /** Resolve the raw template (no placeholder substitution). */
    ConfiguredMessage resolve(MessageKey key);

    /** Resolve and substitute {@code %placeholder%} tokens from {@code replacements}. */
    ResolvedMessage resolve(MessageKey key, Map<String, String> replacements);

    /** Re-read {@code messages.yml} and swap in the new catalog atomically. */
    void reload();
}
