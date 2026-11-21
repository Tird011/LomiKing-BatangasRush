package com.lomiking.core.food;

/** Meats. Unlike other ingredients the cut style is chosen per item (liver is sliced, chicken chopped, tapa fried). */
public class Protein extends Ingredient {
    private final PrepMethod cut;
    public Protein(String id, String displayName, double unitCost, PrepMethod cut) { super(id, displayName, unitCost); this.cut = cut; }
    @Override public IngredientCategory getCategory() { return IngredientCategory.PROTEIN; }
    @Override public PrepMethod getPrepMethod() { return cut; }
    @Override public boolean isPerishable() { return true; }
}
