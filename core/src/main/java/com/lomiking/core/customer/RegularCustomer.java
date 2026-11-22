package com.lomiking.core.customer;

import com.lomiking.core.food.Dish;
import com.lomiking.core.food.Menu;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** The suki: average patience, orders anything, tips when happy. */
public class RegularCustomer extends Customer {
    public RegularCustomer(String id, String name) { super(id, name); }
    @Override public String getTypeLabel() { return "Suki"; }
    @Override public String getModelId() { return "customer_suki"; }
    @Override protected double basePatience() { return 60; }
    @Override protected List<Dish> chooseDishes(Menu menu, Random rng) {
        List<Dish> out = new ArrayList<>();
        out.add(pick(menu.all(), rng));
        if (menu.has("lumpiang_shanghai") && rng.nextDouble() < 0.25) out.add(menu.get("lumpiang_shanghai"));
        return out;
    }
    @Override protected double tipRate(int stars) { return stars >= 4 ? 0.1 : 0; }
}
