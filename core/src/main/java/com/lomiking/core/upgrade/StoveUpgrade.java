package com.lomiking.core.upgrade;

/** A better stove heats faster and makes timing taps more forgiving. */
public class StoveUpgrade extends Upgrade {
    public StoveUpgrade() { super("stove", "Turbo Kalan", "Hotter fire, easier timing.", 3, 4, 200, 2.0); }
    @Override public String effect(int level) { return "Heat +" + (25 * level) + "%, timing window +" + (30 * level) + "%"; }
}
