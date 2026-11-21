package com.lomiking.core.food;

/** Onions, garlic, cabbage, carrots, green onions. */
public class Vegetable extends Ingredient {
    public Vegetable(String id, String displayName, double unitCost) { super(id, displayName, unitCost); }
    @Override public IngredientCategory getCategory() { return IngredientCategory.VEGETABLE; }
    @Override public PrepMethod getPrepMethod() { return PrepMethod.CHOP; }
    @Override public boolean isPerishable() { return true; }
}
