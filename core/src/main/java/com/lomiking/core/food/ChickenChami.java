package com.lomiking.core.food;

import static com.lomiking.core.food.IngredientCatalog.*;

import com.lomiking.core.cooking.*;
import java.util.Arrays;
import java.util.List;

/** Sweet-spicy stir-fried noodles with chicken and vegetables, cooked over a roaring wok fire. */
public class ChickenChami extends Dish {
    public ChickenChami() { super("chicken_chami", "Chicken Chami", 95); }
    @Override public DishCategory getCategory() { return DishCategory.STIR_FRY; }
    @Override public List<String> getIngredientIds() {
        return Arrays.asList(NOODLES, CHICKEN, CABBAGE, CARROT, GARLIC, ONION, OIL, SOY_SAUCE, OYSTER_SAUCE, SUGAR, CHILI_FLAKES);
    }
    @Override protected List<CookingStep> buildSteps(KitchenPerks p) {
        return Arrays.asList(
            new ChopStep("chicken", CHICKEN, p.cuts(3)),
            new ChopStep("cabbage", CABBAGE, p.cuts(3)),
            new ChopStep("carrot", CARROT, p.cuts(2)),
            new FanFireStep("Get the wok screaming hot", p.zoneTime(2.5), p.heatGain()),
            new AddIngredientsStep("Saute the chicken", Arrays.asList(OIL, GARLIC, ONION, CHICKEN)),
            new AddIngredientsStep("Toss in veggies and noodles", Arrays.asList(CABBAGE, CARROT, NOODLES)),
            new AddIngredientsStep("Pour the chami sauce", Arrays.asList(SOY_SAUCE, OYSTER_SAUCE)),
            new SeasonStep("sugar", SUGAR, 2),
            new SeasonStep("chili flakes", CHILI_FLAKES, 1),
            new StirStep("Toss everything together", 4));
    }
    @Override public double getDeliveryFragility() { return 0.35; }
    @Override public List<String> getPossibleExtras() { return Arrays.asList(CALAMANSI, SILI); }
}
