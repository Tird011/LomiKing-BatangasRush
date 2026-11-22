package com.lomiking.core.customer;

import com.lomiking.core.food.Dish;
import com.lomiking.core.food.Menu;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Picky about taste, patient, always wants extras, and tips big when impressed. */
public class TitaCustomer extends Customer {
    public TitaCustomer(String id, String name) { super(id, name); }
    @Override public String getTypeLabel() { return "Tita"; }
    @Override public String getModelId() { return "customer_tita"; }
    @Override protected double basePatience() { return 75; }
    @Override protected List<Dish> chooseDishes(Menu menu, Random rng) {
        return Collections.singletonList(menu.getOrAny(rng.nextBoolean() && menu.has("lomi_overload") ? "lomi_overload" : "lomi_batangas", rng));
    }
    @Override protected double chanceOfExtra() { return 0.9; }
    @Override protected double qualityWeight() { return 0.85; }
    @Override protected double tipRate(int stars) { return stars == 5 ? 0.2 : stars == 4 ? 0.1 : 0; }
    @Override public String greeting() { return "Iho, yung lomi ha, malapot!"; }
    @Override protected String comment(int stars) {
        return stars >= 5 ? "Ay, parang luto ng lola ko!" : stars <= 2 ? "Kulang sa lasa, iho." : super.comment(stars);
    }
}
