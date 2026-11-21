package com.lomiking.core.food;

/** Fresh egg noodles (miki / lomi noodles). */
public class Noodle extends Ingredient {
    public Noodle(String id, String displayName, double unitCost) { super(id, displayName, unitCost); }
    @Override public IngredientCategory getCategory() { return IngredientCategory.NOODLE; }
    @Override public PrepMethod getPrepMethod() { return PrepMethod.BOIL; }
    @Override public boolean isPerishable() { return true; }
}
