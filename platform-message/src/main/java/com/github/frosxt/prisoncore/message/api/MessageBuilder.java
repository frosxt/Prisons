package com.github.frosxt.prisoncore.message.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class MessageBuilder {

    private final MessageService service;
    private Object sender;
    private UUID playerId;
    private boolean broadcastMode;
    private String rawText;
    private MessageKey messageKey;
    private final Map<String, String> replacements = new LinkedHashMap<>();

    private MessageBuilder(final MessageService service) {
        this.service = Objects.requireNonNull(service, "service");
    }

    public static MessageBuilder create(final MessageService service) {
        return new MessageBuilder(service);
    }

    public MessageBuilder to(final UUID playerId) {
        this.playerId = Objects.requireNonNull(playerId, "playerId");
        this.sender = null;
        this.broadcastMode = false;
        return this;
    }

    public MessageBuilder to(final Object sender) {
        this.sender = Objects.requireNonNull(sender, "sender");
        this.playerId = null;
        this.broadcastMode = false;
        return this;
    }

    public MessageBuilder toAll() {
        this.broadcastMode = true;
        this.playerId = null;
        this.sender = null;
        return this;
    }

    public MessageBuilder text(final String message) {
        this.rawText = Objects.requireNonNull(message, "message");
        this.messageKey = null;
        return this;
    }

    public MessageBuilder key(final MessageKey key) {
        this.messageKey = Objects.requireNonNull(key, "key");
        this.rawText = null;
        return this;
    }

    public MessageBuilder replace(final String placeholder, final String value) {
        this.replacements.put(
                Objects.requireNonNull(placeholder, "placeholder"),
                Objects.requireNonNull(value, "value")
        );
        return this;
    }

    public void send() {
        if (rawText == null && messageKey == null) {
            throw new IllegalStateException("Either text or key must be set before sending");
        }

        final Map<String, String> reps = replacements.isEmpty()
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(replacements);

        if (messageKey != null) {
            if (broadcastMode) {
                service.broadcast(messageKey, reps);
            } else if (sender != null) {
                service.send(sender, messageKey, reps);
            } else if (playerId != null) {
                service.send(playerId, messageKey, reps);
            } else {
                throw new IllegalStateException("Target must be set via to() or toAll()");
            }
        } else {
            if (broadcastMode) {
                throw new IllegalStateException("Raw text broadcast not supported — use a MessageKey");
            }
            if (playerId != null) {
                service.send(playerId, rawText);
            } else {
                throw new IllegalStateException("Raw text requires a player target via to(UUID)");
            }
        }
    }
}
