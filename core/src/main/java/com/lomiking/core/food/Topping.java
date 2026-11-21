package com.lomiking.core.food;

/** Ready-made toppings: chicharon, kikiam, fish ball, squid ball, quail egg. */
public class Topping extends Ingredient {
    public Topping(String id, String displayName, double unitCost) { super(id, displayName, unitCost); }
    @Override public IngredientCategory getCategory() { return IngredientCategory.TOPPING; }
    @Override public PrepMethod getPrepMethod() { return PrepMethod.PLACE; }
}
