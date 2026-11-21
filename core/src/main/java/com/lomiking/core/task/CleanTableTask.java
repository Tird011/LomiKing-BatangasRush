package com.lomiking.core.task;

import com.lomiking.core.cooking.InputType;
import com.lomiking.core.cooking.PlayerInput;
import com.lomiking.core.engine.GameContext;
import com.lomiking.core.event.EventType;
import com.lomiking.core.shop.Table;
import java.util.Map;

/** Wipe a messy table with swipes. Customers can't sit at a dirty table. */
public class CleanTableTask extends Task {
    private final int tableId;
    private double startDirt = -1;
    private double currentDirt = 1;

    public CleanTableTask(String id, int tableId) {
        super(id, "Wipe table " + tableId, Sector.MAIN_DINER, 5, -1);
        this.tableId = tableId;
    }
    @Override public boolean isInteractive() { return true; }
    @Override public double getProgress() { return startDirt <= 0 ? 0 : 1 - currentDirt / startDirt; }
    @Override protected boolean isGoalMet(GameContext ctx) {
        Table t = ctx.shop().getTable(tableId);
        if (startDirt < 0) startDirt = Math.max(t.getDirt(), 0.0001);
        currentDirt = t.getDirt();
        return t.isClean();
    }
    @Override protected void onInput(PlayerInput in, GameContext ctx) {
        if (in.getType() != InputType.SWIPE) return;
        Table t = ctx.shop().getTable(tableId);
        t.wipe(0.1 + 0.25 * Math.min(1, in.getValue()));
        currentDirt = t.getDirt();
        ctx.emit(EventType.TASK_PROGRESS, "taskId", getId(), "progress", getProgress());
    }
    public int getTableId() { return tableId; }
    @Override protected void fillViewState(Map<String, Object> m) { m.put("tableId", tableId); m.put("dirt", Math.round(currentDirt * 100) / 100.0); }
}
