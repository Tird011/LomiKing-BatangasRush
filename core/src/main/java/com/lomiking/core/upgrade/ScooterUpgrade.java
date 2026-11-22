package com.lomiking.core.upgrade;

/** A better scooter: faster deliveries, fewer spills. */
public class ScooterUpgrade extends Upgrade {
    public ScooterUpgrade() { super("scooter", "Bagong Motor", "Faster rides, fewer spills.", 3, 7, 300, 1.9); }
    @Override public String effect(int level) { return "Speed +" + (15 * level) + "%, spills -" + (20 * level) + "%"; }
}
