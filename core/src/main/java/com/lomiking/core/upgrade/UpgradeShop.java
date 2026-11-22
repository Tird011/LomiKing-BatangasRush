package com.lomiking.core.upgrade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** All upgrades and the levels the player owns. */
public class UpgradeShop {
    public static final String TABLES = "tables", KNIFE = "knife", STOVE = "stove", DECOR = "decor",
            SUPPLIER = "supplier", SCOOTER = "scooter", SIGN = "sign", RECIPE = "recipe";

    private final Map<String, Upgrade> upgrades = new LinkedHashMap<>();
    private final Map<String, Integer> levels = new LinkedHashMap<>();

    public UpgradeShop() {
        for (Upgrade u : new Upgrade[]{new TablesUpgrade(), new KnifeUpgrade(), new StoveUpgrade(), new DecorUpgrade(),
                new SupplierUpgrade(), new ScooterUpgrade(), new SignUpgrade(), new RecipeBookUpgrade()}) {
            upgrades.put(u.getId(), u);
            levels.put(u.getId(), 0);
        }
    }

    public int level(String id) { return levels.getOrDefault(id, 0); }
    public Upgrade get(String id) { return upgrades.get(id); }
    public List<Upgrade> all() { return Collections.unmodifiableList(new ArrayList<>(upgrades.values())); }

    /** Price of the next level, or -1 if maxed/unknown. */
    public double nextCost(String id) {
        Upgrade u = upgrades.get(id);
        if (u == null || level(id) >= u.getMaxLevel()) return -1;
        return u.cost(level(id) + 1);
    }

    /** Raises the level by one (payment is the engine's job). */
    public void raise(String id) {
        Upgrade u = upgrades.get(id);
        if (u == null) throw new IllegalArgumentException("Unknown upgrade " + id);
        if (level(id) >= u.getMaxLevel()) throw new IllegalStateException(u.getName() + " is maxed");
        levels.put(id, level(id) + 1);
    }

    public void setLevel(String id, int lvl) { if (upgrades.containsKey(id)) levels.put(id, Math.max(0, Math.min(lvl, upgrades.get(id).getMaxLevel()))); }
    public Map<String, Integer> levels() { return Collections.unmodifiableMap(levels); }
}
