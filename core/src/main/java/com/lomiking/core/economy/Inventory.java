package com.lomiking.core.economy;

import com.lomiking.core.food.Ingredient;
import com.lomiking.core.food.IngredientCatalog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The pantry: portions of each ingredient in stock. */
public class Inventory {
    private final Map<String, Integer> stock = new LinkedHashMap<>();

    public Inventory(IngredientCatalog catalog, int startingPortions) {
        for (Ingredient i : catalog.all()) stock.put(i.getId(), startingPortions);
    }

    public int get(String id) { return stock.getOrDefault(id, 0); }

    public void add(String id, int qty) {
        if (qty < 0) throw new IllegalArgumentException("negative qty");
        stock.merge(id, qty, Integer::sum);
    }

    /** Ingredient ids we don't have enough of for this list (duplicates count twice). */
    public List<String> missingFor(Collection<String> ids) {
        Map<String, Integer> need = new HashMap<>();
        for (String id : ids) need.merge(id, 1, Integer::sum);
        List<String> missing = new ArrayList<>();
        for (Map.Entry<String, Integer> e : need.entrySet()) if (get(e.getKey()) < e.getValue()) missing.add(e.getKey());
        Collections.sort(missing);
        return missing;
    }

    /** Takes one portion of each id. Fails without changing anything if something is missing. */
    public boolean consume(Collection<String> ids) {
        if (!missingFor(ids).isEmpty()) return false;
        for (String id : ids) stock.merge(id, -1, Integer::sum);
        return true;
    }

    public List<String> lowStock(int threshold) {
        List<String> low = new ArrayList<>();
        for (Map.Entry<String, Integer> e : stock.entrySet()) if (e.getValue() <= threshold) low.add(e.getKey());
        return low;
    }

    public Map<String, Integer> view() { return Collections.unmodifiableMap(stock); }
}
