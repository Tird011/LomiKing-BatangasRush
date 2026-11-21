package com.lomiking.core.food;

/** Eggs: beaten into lomi or fried sunny-side up for silog. */
public class EggIngredient extends Ingredient {
    public EggIngredient(String id, String displayName, double unitCost) { super(id, displayName, unitCost); }
    @Override public IngredientCategory getCategory() { return IngredientCategory.EGG; }
    @Override public PrepMethod getPrepMethod() { return PrepMethod.CRACK; }
    @Override public boolean isPerishable() { return true; }
}
