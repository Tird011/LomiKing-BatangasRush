package com.lomiking.core.scene;

import com.lomiking.core.cooking.PlayerInput;
import com.lomiking.core.engine.GameEngine;
import com.lomiking.core.task.Task;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** The dining area: wipe tables, wash dishes, sweep. One chore at a time. */
public class MainDinerScene extends Scene {
    private Task activeTask;

    @Override public SceneId getId() { return SceneId.MAIN_DINER; }
    @Override public Set<SceneId> getExits() { return EnumSet.of(SceneId.COUNTER); }
    @Override public String getEnvironmentModel() { return "diner_tables"; }

    @Override public boolean handleInput(PlayerInput input, GameEngine engine) {
        if (activeTask == null || activeTask.isFinished()) return false;
        activeTask.handleInput(input, engine);
        return true;
    }
    @Override public void onExit(GameEngine engine) {
        if (activeTask != null) activeTask.deactivate();
        activeTask = null;
    }

    public Task getActiveTask() { return activeTask; }
    public void setActiveTask(Task t) {
        if (activeTask != null && activeTask != t) activeTask.deactivate();
        activeTask = t;
        if (t != null) t.activate();
    }
    public void clearIfActive(Task t) { if (activeTask == t) activeTask = null; }

    @Override public Map<String, Object> viewState(GameEngine engine) {
        Map<String, Object> m = super.viewState(engine);
        if (activeTask != null) m.put("activeTask", activeTask.getId());
        return m;
    }
}
