package com.lomiking.core.upgrade;

/** Cozy decor (banderitas, plants, a videoke!) keeps customers patient and generous. */
public class DecorUpgrade extends Upgrade {
    public DecorUpgrade() { super("decor", "Pinoy Decor", "Customers wait longer and tip more.", 4, 5, 180, 1.9); }
    @Override public String effect(int level) { return "Patience +" + (15 * level) + "%, tips +" + (5 * level) + "%"; }
}
