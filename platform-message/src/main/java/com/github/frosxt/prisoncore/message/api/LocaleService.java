package com.github.frosxt.prisoncore.message.api;

import com.github.frosxt.prisoncore.message.api.model.ResolvedMessage;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface LocaleService {

    ResolvedMessage resolve(MessageKey key, String locale);

    ResolvedMessage resolve(MessageKey key, String locale, Map<String, String> replacements);

    ResolvedMessage resolve(MessageKey key, UUID playerId);

    ResolvedMessage resolve(MessageKey key, UUID playerId, Map<String, String> replacements);

    void setPlayerLocale(UUID playerId, String locale);

    String getPlayerLocale(UUID playerId);

    String defaultLocale();

    Set<String> availableLocales();

    void reload();
}
