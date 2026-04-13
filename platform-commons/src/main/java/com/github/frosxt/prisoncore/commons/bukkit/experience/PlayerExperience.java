package com.github.frosxt.prisoncore.commons.bukkit.experience;

import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * Wraps a Bukkit {@link Player} to provide convenient experience manipulation.
 * Instantiated per-use — not a utility class.
 */
public final class PlayerExperience {
    private final Player player;

    public PlayerExperience(final Player player) {
        this.player = player;
    }

    /**
     * Calculates the player's accurate total XP from their current level and progress.
     *
     * @return total experience points
     */
    public int totalXp() {
        final int level = this.player.getLevel();
        final int base = ExperienceCalculator.totalXpAtLevel(level);
        final int levelCost = ExperienceCalculator.xpRequiredForLevel(level + 1);
        return base + Math.round(this.player.getExp() * levelCost);
    }

    /**
     * Sets the player's experience to an exact total XP value.
     *
     * @param xp the total XP to set (clamped to 0 minimum)
     */
    public void setTotalXp(final int xp) {
        final int clamped = Math.max(0, xp);
        final int level = ExperienceCalculator.levelFromTotalXp(clamped);
        final int base = ExperienceCalculator.totalXpAtLevel(level);
        final int levelCost = ExperienceCalculator.xpRequiredForLevel(level + 1);

        this.player.setLevel(level);
        this.player.setExp(levelCost > 0 ? (float) (clamped - base) / levelCost : 0.0f);
    }

    /**
     * Adds XP to the player.
     *
     * @param amount the amount to add (must be non-negative)
     */
    public void addXp(final int amount) {
        if (amount <= 0) {
            return;
        }
        setTotalXp(totalXp() + amount);
    }

    /**
     * Removes XP from the player, flooring at zero.
     *
     * @param amount the amount to remove (must be non-negative)
     */
    public void removeXp(final int amount) {
        if (amount <= 0) {
            return;
        }
        setTotalXp(totalXp() - amount);
    }

    /**
     * Checks whether the player has at least the specified amount of XP.
     *
     * @param amount the amount to check
     * @return true if the player has enough XP
     */
    public boolean hasXp(final int amount) {
        return totalXp() >= amount;
    }

    /**
     * Returns the XP remaining until the player reaches the next level.
     *
     * @return XP needed for the next level-up
     */
    public int xpToNextLevel() {
        final int levelCost = ExperienceCalculator.xpRequiredForLevel(this.player.getLevel() + 1);
        return Math.round(levelCost * (1.0f - this.player.getExp()));
    }

    /**
     * Returns the player's progress within the current level.
     *
     * @return a value between 0.0 and 1.0
     */
    public float progress() {
        return this.player.getExp();
    }

    /**
     * Sets the player's level and resets progress to zero.
     *
     * @param level the level to set
     */
    public void setLevel(final int level) {
        this.player.setLevel(Math.max(0, level));
        this.player.setExp(0.0f);
    }

    /**
     * Adds levels to the player's current level.
     *
     * @param levels the number of levels to add
     */
    public void addLevels(final int levels) {
        if (levels <= 0) {
            return;
        }
        this.player.setLevel(this.player.getLevel() + levels);
    }

    /**
     * Removes levels from the player, flooring at zero.
     *
     * @param levels the number of levels to remove
     */
    public void removeLevels(final int levels) {
        if (levels <= 0) {
            return;
        }
        this.player.setLevel(Math.max(0, this.player.getLevel() - levels));
    }

    /**
     * Resets the player to level 0 with zero XP.
     */
    public void reset() {
        this.player.setLevel(0);
        this.player.setExp(0.0f);
    }

    /**
     * Returns a 20-character colored progress bar for the current level.
     *
     * @return the formatted progress bar
     */
    public String progressBar() {
        return progressBar(20);
    }

    /**
     * Returns a colored progress bar of the specified length for the current level.
     *
     * @param length the number of bar characters
     * @return the formatted progress bar
     */
    public String progressBar(final int length) {
        return ExperienceCalculator.progressBar(this.player.getExp(), length);
    }

    /**
     * Returns a detailed info string in the format:
     * {@code Level {level} ({xp}/{xpToNext} XP) [{bar}]}
     *
     * @return the formatted detail string
     */
    public String detailedInfo() {
        final int level = this.player.getLevel();
        final int levelCost = ExperienceCalculator.xpRequiredForLevel(level + 1);
        final int currentXpInLevel = Math.round(this.player.getExp() * levelCost);
        return "Level " + level + " (" + currentXpInLevel + "/" + levelCost + " XP) [" + progressBar() + "]";
    }

    /**
     * Transfers XP from one player to another.
     *
     * @param from   the source player
     * @param to     the destination player
     * @param amount the amount of XP to transfer
     */
    public static void transfer(final Player from, final Player to, final int amount) {
        if (amount <= 0) {
            return;
        }
        final PlayerExperience source = new PlayerExperience(from);
        final int available = Math.min(amount, source.totalXp());
        source.removeXp(available);
        new PlayerExperience(to).addXp(available);
    }

    /**
     * Distributes XP evenly across a collection of players.
     * Any remainder from integer division is discarded.
     *
     * @param players the players to receive XP
     * @param totalXp the total XP to distribute
     */
    public static void distribute(final Collection<Player> players, final int totalXp) {
        if (players.isEmpty() || totalXp <= 0) {
            return;
        }
        final int share = totalXp / players.size();
        for (final Player player : players) {
            new PlayerExperience(player).addXp(share);
        }
    }
}
