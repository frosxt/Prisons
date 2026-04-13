package com.github.frosxt.prisoncore.commons.bukkit.experience;

public final class ExperienceCalculator {
    private ExperienceCalculator() {
        throw new UnsupportedOperationException("Utility classes cannot be instantiated");
    }

    /**
     * Returns the total XP accumulated to reach the given level (with zero progress).
     *
     * @param level the target level (must be non-negative)
     * @return total XP required to reach the level from level 0
     */
    public static int totalXpAtLevel(final int level) {
        if (level <= 0) {
            return 0;
        }
        if (level <= 16) {
            return level * level + 6 * level;
        }
        if (level <= 31) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        }
        return (int) (4.5 * level * level - 162.5 * level + 2220);
    }

    /**
     * Returns the XP required to advance from {@code level - 1} to {@code level}.
     *
     * @param level the target level (must be positive)
     * @return XP cost for that single level-up
     */
    public static int xpRequiredForLevel(final int level) {
        if (level <= 0) {
            return 0;
        }
        if (level <= 16) {
            return 2 * level + 7;
        }
        if (level <= 31) {
            return 5 * level - 38;
        }
        return 9 * level - 158;
    }

    /**
     * Calculates the level reached given a total XP amount.
     *
     * @param totalXp the total experience points (must be non-negative)
     * @return the highest level fully reached
     */
    public static int levelFromTotalXp(final int totalXp) {
        if (totalXp <= 0) {
            return 0;
        }

        if (totalXp <= 352) {
            return (int) Math.floor(-3.0 + Math.sqrt(9.0 + totalXp));
        }

        if (totalXp <= 1507) {
            return (int) Math.floor(8.1 + Math.sqrt(0.4 * (totalXp - 195.975)));
        }

        return (int) Math.floor(18.0556 + Math.sqrt(0.2222 * (totalXp - 752.986)));
    }

    /**
     * Returns the total XP needed to go from {@code fromLevel} to {@code toLevel}.
     *
     * @param fromLevel the starting level
     * @param toLevel   the ending level
     * @return the XP difference (negative if {@code toLevel < fromLevel})
     */
    public static int xpForRange(final int fromLevel, final int toLevel) {
        return totalXpAtLevel(toLevel) - totalXpAtLevel(fromLevel);
    }

    /**
     * Builds a colored progress bar string using legacy color codes.
     * Filled segments use {@code &a|} and empty segments use {@code &7|}.
     *
     * @param progress a value between 0.0 and 1.0
     * @param length   the total number of bar characters
     * @return the formatted progress bar string
     */
    public static String progressBar(final float progress, final int length) {
        final float clamped = Math.max(0.0f, Math.min(1.0f, progress));
        final int filled = Math.round(clamped * length);

        return "&a" +
                "|".repeat(Math.max(0, filled)) +
                "&7" +
                "|".repeat(Math.max(0, length - filled));
    }
}
