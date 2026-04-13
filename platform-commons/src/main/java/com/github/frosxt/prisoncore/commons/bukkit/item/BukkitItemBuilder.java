package com.github.frosxt.prisoncore.commons.bukkit.item;

import com.github.frosxt.prisoncore.commons.bukkit.color.ColorTranslator;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Fluent builder for {@link ItemStack}. Handles color-coded names/lore, enchantments,
 * item flags, skull textures, PDC writes, and potion meta in a single chain.
 * Strings passed to {@link #name} and {@link #lore} are automatically colorized.
 */
public final class BukkitItemBuilder {
    private final ItemStack item;
    private final ItemMeta meta;
    private final List<Consumer<PersistentDataContainer>> pendingPdcOperations = new ArrayList<>();

    private BukkitItemBuilder(final Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    private BukkitItemBuilder(final ItemStack source) {
        this.item = source.clone();
        this.meta = item.getItemMeta();
    }

    public static BukkitItemBuilder of(final Material material) {
        return new BukkitItemBuilder(material);
    }

    public static BukkitItemBuilder of(final ItemStack item) {
        return new BukkitItemBuilder(item);
    }

    public BukkitItemBuilder name(final String name) {
        meta.setDisplayName(ColorTranslator.colorize(name));
        return this;
    }

    public BukkitItemBuilder lore(final String... lines) {
        final List<String> lore = new ArrayList<>();
        for (final String line : lines) {
            lore.add(ColorTranslator.colorize(line));
        }
        meta.setLore(lore);
        return this;
    }

    public BukkitItemBuilder lore(final List<String> lines) {
        final List<String> lore = new ArrayList<>();
        for (final String line : lines) {
            lore.add(ColorTranslator.colorize(line));
        }
        meta.setLore(lore);
        return this;
    }

    public BukkitItemBuilder addLore(final String... lines) {
        final List<String> existing = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        for (final String line : lines) {
            existing.add(ColorTranslator.colorize(line));
        }
        meta.setLore(existing);
        return this;
    }

    public BukkitItemBuilder addLore(final List<String> lines) {
        final List<String> existing = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        for (final String line : lines) {
            existing.add(ColorTranslator.colorize(line));
        }
        meta.setLore(existing);
        return this;
    }

    public BukkitItemBuilder amount(final int amount) {
        item.setAmount(amount);
        return this;
    }

    public BukkitItemBuilder enchant(final Enchantment enchantment, final int level) {
        meta.addEnchant(enchantment, level, true);
        return this;
    }

    public BukkitItemBuilder enchant(final Enchantment enchantment) {
        meta.addEnchant(enchantment, 1, true);
        return this;
    }

    public BukkitItemBuilder glow() {
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    public BukkitItemBuilder flags(final ItemFlag... flags) {
        meta.addItemFlags(flags);
        return this;
    }

    public BukkitItemBuilder hideFlags() {
        meta.addItemFlags(ItemFlag.values());
        return this;
    }

    public BukkitItemBuilder customModelData(final int data) {
        meta.setCustomModelData(data);
        return this;
    }

    public BukkitItemBuilder unbreakable(final boolean unbreakable) {
        meta.setUnbreakable(unbreakable);
        return this;
    }

    public BukkitItemBuilder durability(final short durability) {
        if (meta instanceof final Damageable damageable) {
            damageable.setDamage(durability);
        }
        return this;
    }

    public BukkitItemBuilder skullOwner(final String name) {
        if (item.getType() == Material.PLAYER_HEAD && meta instanceof final SkullMeta skullMeta) {
            SkullTextureApplier.applyOwner(skullMeta, name);
        }
        return this;
    }

    public BukkitItemBuilder skullTexture(final String texture) {
        if (item.getType() == Material.PLAYER_HEAD && meta instanceof final SkullMeta skullMeta) {
            SkullTextureApplier.applyTexture(skullMeta, texture);
        }
        return this;
    }

    public BukkitItemBuilder leatherColor(final Color color) {
        if (meta instanceof final LeatherArmorMeta leatherArmorMeta) {
            leatherArmorMeta.setColor(color);
        }
        return this;
    }

    public BukkitItemBuilder leatherColor(final int r, final int g, final int b) {
        return leatherColor(Color.fromRGB(r, g, b));
    }

    public BukkitItemBuilder potionEffect(final PotionEffectType type, final int duration, final int amplifier) {
        if (meta instanceof final PotionMeta potionMeta) {
            potionMeta.addCustomEffect(new PotionEffect(type, duration, amplifier), true);
        }
        return this;
    }

    public BukkitItemBuilder potionColor(final Color color) {
        if (meta instanceof final PotionMeta potionMeta) {
            potionMeta.setColor(color);
        }
        return this;
    }

    public BukkitItemBuilder nbtString(final String key, final String value) {
        pendingPdcOperations.add(pdc -> PersistentDataAdapter.setString(pdc, key, value));
        return this;
    }

    public BukkitItemBuilder nbtInt(final String key, final int value) {
        pendingPdcOperations.add(pdc -> PersistentDataAdapter.setInt(pdc, key, value));
        return this;
    }

    public BukkitItemBuilder nbtBoolean(final String key, final boolean value) {
        pendingPdcOperations.add(pdc -> PersistentDataAdapter.setBoolean(pdc, key, value));
        return this;
    }

    public ItemStack build() {
        if (!pendingPdcOperations.isEmpty()) {
            final PersistentDataContainer pdc = meta.getPersistentDataContainer();
            for (final Consumer<PersistentDataContainer> operation : pendingPdcOperations) {
                operation.accept(pdc);
            }
        }
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack buildSingle() {
        item.setAmount(1);
        return build();
    }
}
