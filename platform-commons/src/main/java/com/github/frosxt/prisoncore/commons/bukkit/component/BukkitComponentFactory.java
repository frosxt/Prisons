package com.github.frosxt.prisoncore.commons.bukkit.component;

import com.github.frosxt.prisoncore.commons.api.builder.ComponentFactory;
import com.github.frosxt.prisoncore.commons.bukkit.color.ColorTranslator;

public final class BukkitComponentFactory implements ComponentFactory {

    @Override
    public String colorize(final String text) {
        return ColorTranslator.colorize(text);
    }

    @Override
    public String stripColor(final String text) {
        return ColorTranslator.stripColor(text);
    }
}
