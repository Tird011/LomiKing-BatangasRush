package com.lomiking.core.food;

/** Pork-bone stock, the base of the soup. */
public class Broth extends Ingredient {
    public Broth(String id, String displayName, double unitCost) { super(id, displayName, unitCost); }
    @Override public IngredientCategory getCategory() { return IngredientCategory.BROTH; }
    @Override public PrepMethod getPrepMethod() { return PrepMethod.POUR; }
    @Override public boolean isPerishable() { return true; }
}
