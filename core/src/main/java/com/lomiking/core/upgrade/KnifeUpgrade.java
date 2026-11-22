package com.lomiking.core.upgrade;

/** A sharper knife needs fewer cuts when chopping. */
public class KnifeUpgrade extends Upgrade {
    public KnifeUpgrade() { super("knife", "Sharp Itak", "Fewer cuts when chopping.", 2, 3, 120, 2.2); }
    @Override public String effect(int level) { return level + " fewer cut" + (level > 1 ? "s" : "") + " per ingredient"; }
}
