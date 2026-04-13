package com.github.frosxt.prisoncore.message.api;

import java.util.Map;
import java.util.UUID;

/**
 * Delivers configured messages to players and the console. Resolves the keyed
 * template from the {@link MessageCatalog}, applies {@code %placeholder%}
 * replacements, then dispatches across every enabled channel (chat/action bar/title/sound).
 */
public interface MessageService {

    void send(Object sender, MessageKey key);

    void send(Object sender, MessageKey key, Map<String, String> replacements);

    void send(UUID playerId, MessageKey key);

    void send(UUID playerId, MessageKey key, Map<String, String> replacements);

    /** Direct chat-only delivery without going through the catalog. Use sparingly. */
    void send(UUID playerId, String rawChat);

    void broadcast(MessageKey key);

    void broadcast(MessageKey key, Map<String, String> replacements);

    MessageCatalog catalog();
}
