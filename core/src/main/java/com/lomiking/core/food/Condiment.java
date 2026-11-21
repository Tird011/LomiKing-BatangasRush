package com.lomiking.core.food;

/** Served on the side on request: calamansi, siling labuyo, atsara. */
public class Condiment extends Ingredient {
    public Condiment(String id, String displayName, double unitCost) { super(id, displayName, unitCost); }
    @Override public IngredientCategory getCategory() { return IngredientCategory.CONDIMENT; }
    @Override public PrepMethod getPrepMethod() { return PrepMethod.SQUEEZE; }
}
