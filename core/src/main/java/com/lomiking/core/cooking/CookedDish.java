package com.lomiking.core.cooking;

import com.lomiking.core.food.Dish;

/** The result of a finished cooking session. */
public final class CookedDish {
    private final Dish dish;
    private final double quality;   // 0..1
    private final boolean burnt;

    public CookedDish(Dish dish, double quality, boolean burnt) {
        this.dish = dish; this.quality = Math.max(0, Math.min(1, quality)); this.burnt = burnt;
    }
    public Dish getDish() { return dish; }
    public double getQuality() { return quality; }
    public boolean isBurnt() { return burnt; }
    /** 1-3 star cook rating shown on the plate. */
    public int getCookStars() { return quality >= 0.85 ? 3 : quality >= 0.6 ? 2 : 1; }
}
