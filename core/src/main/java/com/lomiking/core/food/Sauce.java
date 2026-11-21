package com.lomiking.core.food;

/** Liquid seasonings poured in: toyo, patis, oyster sauce, vinegar. */
public class Sauce extends Ingredient {
    public Sauce(String id, String displayName, double unitCost) { super(id, displayName, unitCost); }
    @Override public IngredientCategory getCategory() { return IngredientCategory.SAUCE; }
    @Override public PrepMethod getPrepMethod() { return PrepMethod.POUR; }
}
