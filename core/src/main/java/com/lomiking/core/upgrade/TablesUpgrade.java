package com.lomiking.core.upgrade;

/** More tables: seat more customers at once (the diner starts with 2). */
public class TablesUpgrade extends Upgrade {
    public TablesUpgrade() { super("tables", "Extra Table", "Seat one more customer group.", 4, 2, 150, 1.8); }
    @Override public String effect(int level) { return (2 + level) + " tables"; }
}
