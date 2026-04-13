package com.github.frosxt.prisoncore.placeholder.core;

import com.github.frosxt.prisoncore.placeholder.api.PlaceholderContext;
import com.github.frosxt.prisoncore.placeholder.api.PlaceholderResolver;
import com.github.frosxt.prisoncore.placeholder.api.PlaceholderService;
import com.github.frosxt.prisoncore.placeholder.api.ResolutionResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DefaultPlaceholderService implements PlaceholderService {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)}");
    private final Map<String, PlaceholderResolver> resolvers = new ConcurrentHashMap<>();

    @Override
    public String process(final String template, final PlaceholderContext context) {
        if (template == null || template.isEmpty()) {
            return template;
        }
        final Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        final StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            final String token = matcher.group(1);
            final String replacement = resolveToken(context, token);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String resolveToken(final PlaceholderContext context, final String token) {
        final int colonIdx = token.indexOf(':');
        if (colonIdx > 0) {
            final String namespace = token.substring(0, colonIdx);
            final String key = token.substring(colonIdx + 1);
            final PlaceholderResolver resolver = resolvers.get(namespace);
            if (resolver != null) {
                final ResolutionResult result = resolver.resolve(context, key);
                if (result instanceof final ResolutionResult.Resolved r) {
                    return r.value();
                }
            }
        }

        for (final PlaceholderResolver resolver : resolvers.values()) {
            final ResolutionResult result = resolver.resolve(context, token);
            if (result instanceof final ResolutionResult.Resolved r) {
                return r.value();
            }
        }
        return "{" + token + "}";
    }

    @Override
    public void registerResolver(final String namespace, final PlaceholderResolver resolver) {
        resolvers.put(namespace, resolver);
    }

    @Override
    public void unregisterResolver(final String namespace) {
        resolvers.remove(namespace);
    }
}
