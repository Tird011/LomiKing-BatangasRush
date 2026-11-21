package com.lomiking.core.customer;

import com.lomiking.core.cooking.CookedDish;
import com.lomiking.core.food.Dish;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** One dish in an order, plus side extras like "extra calamansi". */
public class OrderLine {
    private final Dish dish;
    private final List<String> extras;
    private CookedDish cooked;
    private boolean cooking;

    public OrderLine(Dish dish, List<String> extras) {
        this.dish = dish;
        this.extras = Collections.unmodifiableList(new ArrayList<>(extras));
    }
    public Dish getDish() { return dish; }
    public List<String> getExtras() { return extras; }
    public CookedDish getCooked() { return cooked; }
    public boolean isCooked() { return cooked != null; }
    public boolean isCooking() { return cooking; }
    void setCooking(boolean c) { cooking = c; }
    void setCooked(CookedDish c) { cooked = c; cooking = false; }
}
