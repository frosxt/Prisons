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

    /**
     * Register a code-owned default message. The operator's {@code messages.yml}
     * still wins over a default with the same key. Modules call this in
     * {@code onPrepare} or {@code onEnable} to ship sane fallbacks so missing
     * keys never surface as "Missing message:" errors at runtime.
     */
    void registerDefault(ConfiguredMessage message);
}
