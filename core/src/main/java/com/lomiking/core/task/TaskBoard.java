package com.lomiking.core.task;

import com.lomiking.core.engine.GameContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The cork board of hanging notes. */
public class TaskBoard {
    private final List<Task> tasks = new ArrayList<>();
    private int counter;

    public String nextId() { return "t" + (++counter); }

    public void post(Task t) { tasks.add(t); }

    public Task get(String id) {
        for (Task t : tasks) if (t.getId().equals(id)) return t;
        return null;
    }

    /** Updates every open note. @return notes that just finished (done or expired). */
    public List<Task> update(double dt, GameContext ctx) {
        List<Task> changed = new ArrayList<>();
        for (Task t : new ArrayList<>(tasks)) if (t.update(dt, ctx)) changed.add(t);
        return changed;
    }

    /** Removes finished notes from the board. */
    public void clearFinished() { tasks.removeIf(Task::isFinished); }

    public List<Task> open() {
        List<Task> out = new ArrayList<>();
        for (Task t : tasks) if (!t.isFinished()) out.add(t);
        return out;
    }
    public List<Task> open(Sector s) {
        List<Task> out = new ArrayList<>();
        for (Task t : tasks) if (!t.isFinished() && t.getSector() == s) out.add(t);
        return out;
    }
    public <T extends Task> boolean hasOpen(Class<T> type) {
        for (Task t : tasks) if (!t.isFinished() && type.isInstance(t)) return true;
        return false;
    }
    public List<Task> all() { return Collections.unmodifiableList(tasks); }
}
