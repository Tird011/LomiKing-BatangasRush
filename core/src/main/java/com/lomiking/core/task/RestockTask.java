package com.lomiking.core.task;

import com.lomiking.core.engine.GameContext;
import java.util.Map;

/** Running low: buy more at the market from the delivery hub. */
public class RestockTask extends Task {
    private final String ingredientId;
    private final int targetQty;
    private int current;

    public RestockTask(String id, String ingredientId, String ingredientName, int targetQty) {
        super(id, "Buy " + ingredientName, Sector.DELIVERY_HUB, 0, -1);
        this.ingredientId = ingredientId; this.targetQty = targetQty;
    }
    @Override public boolean isInteractive() { return false; }
    @Override public double getProgress() { return Math.min(1, (double) current / targetQty); }
    @Override protected boolean isGoalMet(GameContext ctx) {
        current = ctx.inventory().get(ingredientId);
        return current >= targetQty;
    }
    public String getIngredientId() { return ingredientId; }
    @Override protected void fillViewState(Map<String, Object> m) { m.put("ingredientId", ingredientId); m.put("have", current); m.put("target", targetQty); }
}
