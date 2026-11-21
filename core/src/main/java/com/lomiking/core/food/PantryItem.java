package com.lomiking.core.food;

/** Basics that need no prep: cooking oil, lumpia wrappers. */
public class PantryItem extends Ingredient {
    public PantryItem(String id, String displayName, double unitCost) { super(id, displayName, unitCost); }
    @Override public IngredientCategory getCategory() { return IngredientCategory.PANTRY; }
    @Override public PrepMethod getPrepMethod() { return PrepMethod.NONE; }
}
