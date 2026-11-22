package com.lomiking.core.cooking;

/** Upgrade levels that make cooking easier or better. Passed to dishes when they build their steps. */
public final class KitchenPerks {
    public static final KitchenPerks NONE = new KitchenPerks(0, 0, 0);
    private final int knife, stove, recipe;

    public KitchenPerks(int knifeLevel, int stoveLevel, int recipeLevel) { knife = knifeLevel; stove = stoveLevel; recipe = recipeLevel; }

    /** A sharper knife needs fewer cuts (never below 1). */
    public int cuts(int base) { return Math.max(1, base - knife); }
    /** A better stove heats faster... */
    public double heatGain() { return 1 + 0.25 * stove; }
    /** ...and needs less time in the green zone. */
    public double zoneTime(double base) { return base * Math.max(0.4, 1 - 0.15 * stove); }
    /** Timing taps are more forgiving on a better stove. */
    public double timingTolerance() { return 1 + 0.3 * stove; }
    /** The recipe book adds a flat quality bonus. */
    public double qualityBonus() { return 0.05 * recipe; }

    public int getKnife() { return knife; }
    public int getStove() { return stove; }
    public int getRecipe() { return recipe; }
}
