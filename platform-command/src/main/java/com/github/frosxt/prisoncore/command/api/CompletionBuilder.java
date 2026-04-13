package com.github.frosxt.prisoncore.command.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class CompletionBuilder {

    private final List<String> staticSuggestions = new ArrayList<>();
    private final List<Function<CommandContext, List<String>>> dynamicSuppliers = new ArrayList<>();

    private CompletionBuilder() {
    }

    public static CompletionBuilder create() {
        return new CompletionBuilder();
    }

    public CompletionBuilder suggest(final String... values) {
        Collections.addAll(staticSuggestions, values);
        return this;
    }

    public CompletionBuilder suggest(final Collection<String> values) {
        staticSuggestions.addAll(values);
        return this;
    }

    public CompletionBuilder suggestDynamic(final Function<CommandContext, List<String>> supplier) {
        dynamicSuppliers.add(supplier);
        return this;
    }

    public CompletionBuilder suggestIf(final Predicate<CommandContext> condition, final String... values) {
        final List<String> frozen = Collections.unmodifiableList(Arrays.asList(values));
        dynamicSuppliers.add(ctx -> condition.test(ctx) ? frozen : Collections.emptyList());
        return this;
    }

    public CompletionBuilder suggestOnlinePlayers() {
        dynamicSuppliers.add(ctx ->
                Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList())
        );
        return this;
    }

    public CompletionBuilder suggestWorlds() {
        dynamicSuppliers.add(ctx ->
                Bukkit.getWorlds().stream()
                        .map(world -> world.getName())
                        .collect(Collectors.toList())
        );
        return this;
    }

    public CompletionBuilder suggestBoolean() {
        staticSuggestions.add("true");
        staticSuggestions.add("false");
        return this;
    }

    public CompletionBuilder suggestEnum(final Class<? extends Enum<?>> enumClass) {
        for (final Enum<?> constant : enumClass.getEnumConstants()) {
            staticSuggestions.add(constant.name().toLowerCase(Locale.ROOT));
        }
        return this;
    }

    public CompletionBuilder suggestNumbers(final int min, final int max) {
        IntStream.rangeClosed(min, max)
                .mapToObj(String::valueOf)
                .forEach(staticSuggestions::add);
        return this;
    }

    public CompletionBuilder suggestGameModes() {
        staticSuggestions.add("survival");
        staticSuggestions.add("creative");
        staticSuggestions.add("adventure");
        staticSuggestions.add("spectator");
        return this;
    }

    public CompletionProvider build() {
        final List<String> frozenStatic = Collections.unmodifiableList(new ArrayList<>(staticSuggestions));
        final List<Function<CommandContext, List<String>>> frozenDynamic =
                Collections.unmodifiableList(new ArrayList<>(dynamicSuppliers));

        return (context, argIndex) -> {
            final String prefix = context.arg(argIndex);
            final String lowerPrefix = prefix != null ? prefix.toLowerCase(Locale.ROOT) : "";

            final List<String> results = new ArrayList<>(frozenStatic);
            for (final Function<CommandContext, List<String>> supplier : frozenDynamic) {
                results.addAll(supplier.apply(context));
            }

            return results.stream()
                    .filter(s -> s.toLowerCase(Locale.ROOT).startsWith(lowerPrefix))
                    .collect(Collectors.toList());
        };
    }
}
