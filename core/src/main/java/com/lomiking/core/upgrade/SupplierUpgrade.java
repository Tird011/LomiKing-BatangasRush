package com.lomiking.core.upgrade;

/** The morning supplier tops up your pantry before opening. */
public class SupplierUpgrade extends Upgrade {
    public SupplierUpgrade() { super("supplier", "Suki Supplier", "Bigger morning restock, cheaper.", 3, 6, 250, 2.0); }
    @Override public String effect(int level) { return "Morning stock " + (6 + 3 * level) + " each, " + (10 + 5 * level) + "% off"; }
}
