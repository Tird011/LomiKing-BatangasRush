package com.lomiking.core.staff;

import com.lomiking.core.shop.Shop;
import com.lomiking.core.shop.Table;

/** Kuya Dodong: wipes tables, washes the dish pile, sweeps. */
public class CleanerWorker extends Worker {
    private double washTimer;
    public CleanerWorker() { super("cleaner", "Kuya Dodong", StaffRole.CLEANER, "worker_cleaner", 250, 50, 5); }
    @Override public String jobDescription() { return "Wipes tables, washes dishes, sweeps"; }

    @Override protected void work(double dt, StaffContext ctx) {
        Shop shop = ctx.shop();
        for (Table t : shop.getTables()) {
            if (t.isFree() && !t.isClean()) {
                t.wipe(0.22 * speed() * dt);
                activity = "Wiping table " + t.getId(); where = "MAIN_DINER"; targetId = "table" + t.getId();
                return;
            }
        }
        if (shop.getDirtyDishes() > 0) {
            washTimer += dt * speed();
            activity = "Washing dishes (" + shop.getDirtyDishes() + " left)"; where = "MAIN_DINER"; targetId = "sink";
            if (washTimer >= 2.5) { washTimer = 0; shop.washDishes(1); }
            return;
        }
        if (shop.getFloorDirt() > 0.12) {
            shop.sweep(0.05 * speed() * dt);
            activity = "Sweeping the floor"; where = "MAIN_DINER"; targetId = "floor";
            return;
        }
        activity = "Taking a merienda break"; where = "MAIN_DINER"; targetId = "";
    }
}
