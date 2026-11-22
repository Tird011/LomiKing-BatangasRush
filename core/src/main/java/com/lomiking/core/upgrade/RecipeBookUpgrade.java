package com.lomiking.core.upgrade;

/** Lola's recipe book: every dish you cook tastes better. */
public class RecipeBookUpgrade extends Upgrade {
    public RecipeBookUpgrade() { super("recipe", "Recipe ni Lola", "Every dish +5% quality.", 3, 10, 500, 2.0); }
    @Override public String effect(int level) { return "Quality +" + (5 * level) + "%"; }
}
