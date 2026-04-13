package com.github.frosxt.prisoncore.command.api;

import java.util.List;

@FunctionalInterface
public interface CompletionProvider {
    List<String> complete(CommandContext context, int argIndex);
}
