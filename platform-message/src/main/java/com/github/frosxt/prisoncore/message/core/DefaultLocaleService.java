package com.github.frosxt.prisoncore.message.core;

import com.github.frosxt.prisoncore.message.api.LocaleService;
import com.github.frosxt.prisoncore.message.api.MessageCatalog;
import com.github.frosxt.prisoncore.message.api.MessageKey;
import com.github.frosxt.prisoncore.message.api.model.ResolvedMessage;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultLocaleService implements LocaleService {

    private final MessageCatalog catalog;
    private final String defaultLocale;
    private final Map<UUID, String> playerLocales = new ConcurrentHashMap<>();

    public DefaultLocaleService(final MessageCatalog catalog, final String defaultLocale) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.defaultLocale = Objects.requireNonNull(defaultLocale, "defaultLocale");
    }

    @Override
    public ResolvedMessage resolve(final MessageKey key, final String locale) {
        return catalog.resolve(key, Collections.emptyMap());
    }

    @Override
    public ResolvedMessage resolve(final MessageKey key, final String locale,
                                    final Map<String, String> replacements) {
        return catalog.resolve(key, replacements);
    }

    @Override
    public ResolvedMessage resolve(final MessageKey key, final UUID playerId) {
        return resolve(key, getPlayerLocale(playerId));
    }

    @Override
    public ResolvedMessage resolve(final MessageKey key, final UUID playerId,
                                    final Map<String, String> replacements) {
        return resolve(key, getPlayerLocale(playerId), replacements);
    }

    @Override
    public void setPlayerLocale(final UUID playerId, final String locale) {
        playerLocales.put(playerId, locale);
    }

    @Override
    public String getPlayerLocale(final UUID playerId) {
        return playerLocales.getOrDefault(playerId, defaultLocale);
    }

    @Override
    public String defaultLocale() {
        return defaultLocale;
    }

    @Override
    public Set<String> availableLocales() {
        return Set.of(defaultLocale);
    }

    @Override
    public void reload() {
        catalog.reload();
    }
}
