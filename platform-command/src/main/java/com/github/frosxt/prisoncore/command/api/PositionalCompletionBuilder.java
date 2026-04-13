package com.github.frosxt.prisoncore.command.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class PositionalCompletionBuilder {

    private final Map<Integer, CompletionProvider> providers = new HashMap<>();

    private PositionalCompletionBuilder() {
    }

    public static PositionalCompletionBuilder create() {
        return new PositionalCompletionBuilder();
    }

    public PositionalCompletionBuilder forArg(final int index, final CompletionBuilder builder) {
        providers.put(index, builder.build());
        return this;
    }

    public CompletionProvider build() {
        final Map<Integer, CompletionProvider> frozen = Collections.unmodifiableMap(new HashMap<>(providers));

        return (context, argIndex) -> {
            final CompletionProvider provider = frozen.get(argIndex);
            if (provider == null) {
                return Collections.emptyList();
            }
            return provider.complete(context, argIndex);
        };
    }
}
