package com.lomiking.core.customer;

import com.lomiking.core.food.Dish;
import com.lomiking.core.food.Menu;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** On a baon budget: only orders what fits, rarely tips, a bit impatient (class starts soon!). */
public class StudentCustomer extends Customer {
    public static final double BUDGET = 100;
    public StudentCustomer(String id, String name) { super(id, name); }
    @Override public String getTypeLabel() { return "Estudyante"; }
    @Override public String getModelId() { return "customer_student"; }
    @Override protected double basePatience() { return 45; }
    @Override protected List<Dish> chooseDishes(Menu menu, Random rng) {
        List<Dish> ok = menu.affordable(BUDGET);
        return Collections.singletonList(ok.isEmpty() ? menu.all().get(0) : pick(ok, rng));
    }
    @Override protected double tipRate(int stars) { return stars == 5 ? 0.05 : 0; }
    @Override protected double qualityWeight() { return 0.5; }
    @Override public String greeting() { return "Kuya, yung pinakamura po?"; }
}
