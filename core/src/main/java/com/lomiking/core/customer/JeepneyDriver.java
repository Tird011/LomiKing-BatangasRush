package com.lomiking.core.customer;

import com.lomiking.core.food.Dish;
import com.lomiking.core.food.Menu;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** In a hurry between trips: big order, very little patience, cares about speed more than polish. */
public class JeepneyDriver extends Customer {
    public JeepneyDriver(String id, String name) { super(id, name); }
    @Override public String getTypeLabel() { return "Tsuper"; }
    @Override public String getModelId() { return "customer_driver"; }
    @Override protected double basePatience() { return 40; }
    @Override protected List<Dish> chooseDishes(Menu menu, Random rng) {
        List<Dish> out = new ArrayList<>();
        out.add(menu.getOrAny(rng.nextBoolean() ? "tapsilog" : "lomi_overload", rng));
        return out;
    }
    @Override protected double qualityWeight() { return 0.4; }
    @Override protected double eatSeconds() { return 3; }
    @Override protected double tipRate(int stars) { return stars >= 3 ? 0.1 : 0; }
    @Override public String greeting() { return "Boss, yung mabilis lang!"; }
}
