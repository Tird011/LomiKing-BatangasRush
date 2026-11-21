package com.lomiking.core.customer;

import com.lomiking.core.food.Dish;
import com.lomiking.core.food.Menu;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** A phone/online order. Never stands in line; the food must be cooked and then delivered from the hub. */
public class DeliveryCustomer extends Customer {
    private final String address;
    public DeliveryCustomer(String id, String name, String address) { super(id, name); this.address = address; }
    public String getAddress() { return address; }
    @Override public String getTypeLabel() { return "Delivery"; }
    @Override public String getModelId() { return "phone_order"; }
    @Override public boolean isRemote() { return true; }
    @Override protected double basePatience() { return 150; }
    @Override protected List<Dish> chooseDishes(Menu menu, Random rng) {
        List<Dish> out = new ArrayList<>();
        out.add(pick(menu.all(), rng));
        if (rng.nextDouble() < 0.5) out.add(pick(menu.all(), rng));
        return out;
    }
    @Override protected double eatSeconds() { return 0; }
    @Override protected double tipRate(int stars) { return stars >= 4 ? 0.15 : 0; }
}
