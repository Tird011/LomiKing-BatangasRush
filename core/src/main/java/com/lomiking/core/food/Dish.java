package com.lomiking.core.food;

import com.lomiking.core.cooking.CookingStep;
import com.lomiking.core.cooking.KitchenPerks;
import com.lomiking.core.cooking.PlateStep;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A menu item. Each concrete dish knows its ingredients and builds its own list of cooking
 * mini-games (abstraction + polymorphism: the kitchen just runs whatever steps the dish returns).
 */
public abstract class Dish {
    private final String id;
    private final String name;
    private final double price;

    protected Dish(String id, String name, double price) {
        this.id = id; this.name = name; this.price = price;
    }

    public final String getId() { return id; }
    public final String getName() { return name; }
    public double getPrice() { return price; }

    public abstract DishCategory getCategory();
    /** Ingredient ids used once per serving. */
    public abstract List<String> getIngredientIds();
    /** The mini-games for this dish, excluding plating. */
    protected abstract List<CookingStep> buildSteps(KitchenPerks perks);

    /** Full step list: the dish's own steps plus plating with the customer's requested extras. */
    public final List<CookingStep> createSteps(List<String> extras) { return createSteps(extras, KitchenPerks.NONE); }

    public final List<CookingStep> createSteps(List<String> extras, KitchenPerks perks) {
        List<CookingStep> steps = new ArrayList<>(buildSteps(perks));
        steps.add(new PlateStep(extras));
        return Collections.unmodifiableList(steps);
    }

    /** How easily it spills or cools during delivery (soups suffer most). 0..1 */
    public double getDeliveryFragility() { return 0.3; }

    /** Typical side condiments customers may ask for with this dish. */
    public List<String> getPossibleExtras() { return Collections.emptyList(); }

    @Override public String toString() { return name + " (P" + (int) price + ")"; }
}
