package com.lomiking.core.task;

import com.lomiking.core.cooking.InputType;
import com.lomiking.core.cooking.PlayerInput;
import com.lomiking.core.engine.GameContext;
import com.lomiking.core.event.EventType;
import java.util.Map;

/** Tap to scrub; each dish needs a few scrubs. */
public class WashDishesTask extends Task {
    public static final int SCRUBS_PER_DISH = 3;
    private final int dishes;
    private int scrubs;

    public WashDishesTask(String id, int dishes) {
        super(id, "Wash " + dishes + " dishes", Sector.MAIN_DINER, 2 * dishes, -1);
        this.dishes = dishes;
    }
    @Override public boolean isInteractive() { return true; }
    @Override public double getProgress() { return Math.min(1, (double) scrubs / (dishes * SCRUBS_PER_DISH)); }
    @Override protected boolean isGoalMet(GameContext ctx) { return getProgress() >= 1 || ctx.shop().getDirtyDishes() == 0; }
    @Override protected void onInput(PlayerInput in, GameContext ctx) {
        if (in.getType() != InputType.TAP && in.getType() != InputType.CIRCLE) return;
        scrubs++;
        if (scrubs % SCRUBS_PER_DISH == 0) {
            ctx.shop().washDishes(1);
            ctx.emit(EventType.TASK_PROGRESS, "taskId", getId(), "progress", getProgress());
        }
    }
    @Override protected void fillViewState(Map<String, Object> m) { m.put("dishes", dishes); m.put("scrubs", scrubs); }
}
