package com.lomiking.core.food;

import static com.lomiking.core.food.IngredientCatalog.*;

import com.lomiking.core.cooking.*;
import java.util.Arrays;
import java.util.List;

/** Crispy pork spring rolls, the favorite side order. */
public class LumpiangShanghai extends Dish {
    public LumpiangShanghai() { super("lumpiang_shanghai", "Lumpiang Shanghai", 60); }
    @Override public DishCategory getCategory() { return DishCategory.SIDE; }
    @Override public List<String> getIngredientIds() { return Arrays.asList(GROUND_PORK, LUMPIA_WRAPPER, CARROT, ONION, OIL); }
    @Override protected List<CookingStep> buildSteps(KitchenPerks p) {
        return Arrays.asList(
            new ChopStep("carrot", CARROT, p.cuts(2)),
            new AddIngredientsStep("Wrap the filling", Arrays.asList(GROUND_PORK, CARROT, ONION, LUMPIA_WRAPPER)),
            new FryStep("shanghai", 4.0, true, p.timingTolerance()));
    }
    @Override public double getDeliveryFragility() { return 0.15; }
    @Override public List<String> getPossibleExtras() { return Arrays.asList(VINEGAR, SILI); }
}
