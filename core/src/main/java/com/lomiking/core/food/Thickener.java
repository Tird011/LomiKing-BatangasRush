package com.lomiking.core.food;

/** Cornstarch slurry that gives Batangas lomi its thick, silky soup. */
public class Thickener extends Ingredient {
    public Thickener(String id, String displayName, double unitCost) { super(id, displayName, unitCost); }
    @Override public IngredientCategory getCategory() { return IngredientCategory.THICKENER; }
    @Override public PrepMethod getPrepMethod() { return PrepMethod.MIX; }
}
