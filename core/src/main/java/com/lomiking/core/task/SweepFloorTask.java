package com.lomiking.core.task;

import com.lomiking.core.cooking.InputType;
import com.lomiking.core.cooking.PlayerInput;
import com.lomiking.core.engine.GameContext;
import com.lomiking.core.event.EventType;

/** Sweep the floor with circles or swipes of the walis tingting. */
public class SweepFloorTask extends Task {
    private double startDirt = -1, dirt = 1;
    public SweepFloorTask(String id) { super(id, "Sweep the floor", Sector.MAIN_DINER, 8, 90); }
    @Override public boolean isInteractive() { return true; }
    @Override public double getProgress() { return startDirt <= 0 ? 0 : 1 - dirt / startDirt; }
    @Override protected boolean isGoalMet(GameContext ctx) {
        if (startDirt < 0) startDirt = Math.max(ctx.shop().getFloorDirt(), 0.0001);
        dirt = ctx.shop().getFloorDirt();
        return dirt <= 0.0001;
    }
    @Override protected void onInput(PlayerInput in, GameContext ctx) {
        double amount;
        if (in.getType() == InputType.CIRCLE) amount = 0.12 * Math.abs(in.getValue());
        else if (in.getType() == InputType.SWIPE) amount = 0.08 * Math.min(1, in.getValue());
        else return;
        ctx.shop().sweep(amount);
        dirt = ctx.shop().getFloorDirt();
        ctx.emit(EventType.TASK_PROGRESS, "taskId", getId(), "progress", getProgress());
    }
}
