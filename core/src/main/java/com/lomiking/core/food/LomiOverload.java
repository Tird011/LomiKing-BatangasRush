package com.lomiking.core.food;

import static com.lomiking.core.food.IngredientCatalog.*;

import java.util.List;

/** "Lomi Overload": the same lomi with every topping in the house (inheritance: only the toppings change). */
public class LomiOverload extends LomiBatangas {
    public LomiOverload() { super("lomi_overload", "Lomi Overload", 130); }
    @Override protected List<String> toppings() {
        List<String> t = super.toppings();
        t.add(FISH_BALL); t.add(SQUID_BALL); t.add(QUAIL_EGG); t.add(FRIED_GARLIC);
        return t;
    }
}
