package com.lomiking.core.staff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The team: every helper that can be hired, hired or not. */
public class Staff {
    private final List<Worker> workers = new ArrayList<>();

    public Staff() {
        workers.add(new ServerWorker());
        workers.add(new CleanerWorker());
        workers.add(new RiderWorker());
        workers.add(new CookWorker());
    }

    public Worker get(String id) {
        for (Worker w : workers) if (w.getId().equals(id)) return w;
        return null;
    }
    public List<Worker> all() { return Collections.unmodifiableList(workers); }
    public void update(double dt, StaffContext ctx) { for (Worker w : workers) w.update(dt, ctx); }
    public double totalWages() { double s = 0; for (Worker w : workers) s += w.dailyWage(); return s; }
    public boolean isHired(StaffRole r) { for (Worker w : workers) if (w.getRole() == r && w.isHired() && !w.isResting()) return true; return false; }
}
