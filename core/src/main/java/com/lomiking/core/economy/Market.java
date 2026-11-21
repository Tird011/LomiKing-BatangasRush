package com.lomiking.core.economy;

import com.lomiking.core.food.Ingredient;
import com.lomiking.core.food.IngredientCatalog;

/** The palengke run from the delivery hub. Buying in bulk (10+) gets a small discount. */
public class Market {
    public static final int BULK_QTY = 10;
    public static final double BULK_DISCOUNT = 0.9;
    private final IngredientCatalog catalog;

    public Market(IngredientCatalog catalog) { this.catalog = catalog; }

    public double priceOf(String ingredientId, int qty) {
        Ingredient i = catalog.get(ingredientId);
        double total = i.getUnitCost() * qty;
        return qty >= BULK_QTY ? Math.round(total * BULK_DISCOUNT * 100) / 100.0 : total;
    }

    /** @return the amount paid, or -1 if the wallet can't cover it */
    public double buy(String ingredientId, int qty, Wallet wallet, Inventory inventory) {
        if (qty <= 0) throw new IllegalArgumentException("qty must be > 0");
        double price = priceOf(ingredientId, qty);
        if (!wallet.spend(price)) return -1;
        inventory.add(ingredientId, qty);
        return price;
    }
}
