package com.lomiking.core.food;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The dishes the shop sells. */
public final class Menu {
    private final Map<String, Dish> dishes = new LinkedHashMap<>();

    public static Menu standard() {
        Menu m = new Menu();
        m.add(new LomiBatangas());
        m.add(new LomiOverload());
        m.add(new Tapsilog());
        m.add(new ChickenChami());
        m.add(new LumpiangShanghai());
        return m;
    }

    public void add(Dish d) { dishes.put(d.getId(), d); }
    public Dish get(String id) {
        Dish d = dishes.get(id);
        if (d == null) throw new IllegalArgumentException("Not on the menu: " + id);
        return d;
    }
    public List<Dish> all() { return Collections.unmodifiableList(new ArrayList<>(dishes.values())); }

    public List<Dish> affordable(double budget) {
        List<Dish> out = new ArrayList<>();
        for (Dish d : dishes.values()) if (d.getPrice() <= budget) out.add(d);
        return out;
    }
    public List<Dish> byCategory(DishCategory c) {
        List<Dish> out = new ArrayList<>();
        for (Dish d : dishes.values()) if (d.getCategory() == c) out.add(d);
        return out;
    }
}
