package com.lomiking.core.food;

/** Dry seasonings shaken in by tapping: salt, paminta, sugar. */
public class Spice extends Ingredient {
    public Spice(String id, String displayName, double unitCost) { super(id, displayName, unitCost); }
    @Override public IngredientCategory getCategory() { return IngredientCategory.SPICE; }
    @Override public PrepMethod getPrepMethod() { return PrepMethod.SPRINKLE; }
}
