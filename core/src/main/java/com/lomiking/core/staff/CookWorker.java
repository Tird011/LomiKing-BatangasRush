package com.lomiking.core.staff;

import com.lomiking.core.customer.Order;

/** Inday: cooks orders at the second stove. Good food, but Mang Kaloy's own cooking can be better. */
public class CookWorker extends Worker {
    private Order order;
    private int line = -1;
    private double progress, duration;
    public CookWorker() { super("cook", "Inday", StaffRole.COOK, "worker_cook", 450, 80, 9); }
    @Override public String jobDescription() { return "Cooks orders at the second stove"; }

    public double quality() { return Math.min(0.92, 0.66 + 0.09 * (getLevel() - 1)); }

    @Override protected void work(double dt, StaffContext ctx) {
        where = "KITCHEN";
        if (order == null) {
            Object[] pick = ctx.lineForAssistant();
            if (pick == null) { activity = "Prepping garnish"; targetId = ""; return; }
            Order o = (Order) pick[0]; int l = (Integer) pick[1];
            if (!ctx.assistantStart(o, l)) { activity = "Waiting for ingredients"; return; }
            order = o; line = l; progress = 0;
            duration = (14 + 2.0 * o.getLines().get(l).getDish().getIngredientIds().size()) / speed();
            targetId = o.getId();
            ctx.staffEvent(this, "Cooking " + o.getLines().get(l).getDish().getName());
        }
        progress += dt;
        activity = "Cooking " + order.getLines().get(line).getDish().getName() + " (" + (int) Math.min(99, 100 * progress / duration) + "%)";
        if (progress >= duration) {
            ctx.assistantFinish(order, line, quality());
            order = null; line = -1; targetId = "";
        }
    }
    public boolean isBusy() { return order != null; }
    public double getProgressRatio() { return order == null ? 0 : Math.min(1, progress / duration); }
}
