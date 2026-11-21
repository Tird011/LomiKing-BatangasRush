package com.lomiking.core.food;

/** Rice for sinangag (garlic fried rice). */
public class Grain extends Ingredient {
    public Grain(String id, String displayName, double unitCost) { super(id, displayName, unitCost); }
    @Override public IngredientCategory getCategory() { return IngredientCategory.GRAIN; }
    @Override public PrepMethod getPrepMethod() { return PrepMethod.PLACE; }
}
