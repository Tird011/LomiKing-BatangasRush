package com.lomiking.core.upgrade;

/** A bright neon sign brings in more customers (and more money). */
public class SignUpgrade extends Upgrade {
    public SignUpgrade() { super("sign", "Neon Sign", "More customers walk in.", 3, 8, 350, 2.0); }
    @Override public String effect(int level) { return "Customers +" + (12 * level) + "%"; }
}
