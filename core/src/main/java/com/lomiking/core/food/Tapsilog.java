package com.lomiking.core.food;

import static com.lomiking.core.food.IngredientCatalog.*;

import com.lomiking.core.cooking.*;
import java.util.Arrays;
import java.util.List;

/** Tapa + sinangag (garlic rice) + itlog (fried egg). */
public class Tapsilog extends Dish {
    public Tapsilog() { super("tapsilog", "Tapsilog", 110); }
    @Override public DishCategory getCategory() { return DishCategory.SILOG; }
    @Override public List<String> getIngredientIds() { return Arrays.asList(TAPA, RICE, EGG, GARLIC, OIL, SALT); }
    @Override protected List<CookingStep> buildSteps() {
        return Arrays.asList(
            new FryStep("tapa", 4.0, true),
            new AddIngredientsStep("Make the sinangag", Arrays.asList(OIL, GARLIC, RICE)),
            new StirStep("Toss the garlic rice", 2),
            new SeasonStep("salt", SALT, 1),
            new FryStep("egg (sunny-side up)", 3.0, false));
    }
    @Override public double getDeliveryFragility() { return 0.25; }
    @Override public List<String> getPossibleExtras() { return Arrays.asList(VINEGAR, ATSARA, SILI); }
}
