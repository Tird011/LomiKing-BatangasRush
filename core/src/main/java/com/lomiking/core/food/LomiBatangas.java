package com.lomiking.core.food;

import static com.lomiking.core.food.IngredientCatalog.*;

import com.lomiking.core.cooking.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** The classic thick Batangas lomi: miki in a cornstarch-thickened pork broth with egg, liver and toppings. */
public class LomiBatangas extends Dish {
    public LomiBatangas() { this("lomi_batangas", "Lomi Batangas", 85); }
    protected LomiBatangas(String id, String name, double price) { super(id, name, price); }

    @Override public DishCategory getCategory() { return DishCategory.NOODLE_SOUP; }

    /** Toppings dropped on at the end; subclasses add more. */
    protected List<String> toppings() { return new ArrayList<>(Arrays.asList(KIKIAM, CHICHARON, GREEN_ONION)); }

    @Override public List<String> getIngredientIds() {
        List<String> ids = new ArrayList<>(Arrays.asList(NOODLES, PORK_BROTH, PORK, LIVER, EGG, CORNSTARCH, ONION, GARLIC, OIL, SALT, PEPPER));
        for (String t : toppings()) if (!ids.contains(t)) ids.add(t);
        return ids;
    }

    @Override protected List<CookingStep> buildSteps() {
        return Arrays.asList(
            new ChopStep("liver", LIVER, 3),
            new ChopStep("onion", ONION, 3),
            new AddIngredientsStep("Saute garlic, onion and meat", Arrays.asList(OIL, GARLIC, ONION, PORK, LIVER)),
            new FanFireStep("Bring the broth to a boil", 3.0),
            new AddIngredientsStep("Pour in the broth and noodles", Arrays.asList(PORK_BROTH, NOODLES)),
            new BoilStep("noodles", 5.0),
            new AddIngredientsStep("Thicken with cornstarch and egg", Arrays.asList(CORNSTARCH, EGG)),
            new StirStep("Stir until thick and silky", 3),
            new SeasonStep("salt", SALT, 2),
            new SeasonStep("paminta", PEPPER, 1),
            new AddIngredientsStep("Add the toppings", toppings()));
    }

    @Override public double getDeliveryFragility() { return 0.8; }
    @Override public List<String> getPossibleExtras() { return Arrays.asList(CALAMANSI, SILI, SOY_SAUCE); }
}
