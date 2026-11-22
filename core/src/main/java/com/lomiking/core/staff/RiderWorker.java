package com.lomiking.core.staff;

import com.lomiking.core.customer.Order;

/** Mang Tonyo: takes ready delivery orders out on his own motorbike. */
public class RiderWorker extends Worker {
    private Order order;
    private double t, trip;
    private boolean delivered;
    public RiderWorker() { super("rider", "Mang Tonyo", StaffRole.RIDER, "worker_rider", 400, 70, 7); }
    @Override public String jobDescription() { return "Delivers phone orders"; }

    @Override protected void work(double dt, StaffContext ctx) {
        if (order == null) {
            Order o = ctx.readyDeliveryOrder();
            if (o == null || !ctx.riderDepart(o)) { activity = "Waiting by the motor"; where = "DELIVERY_HUB"; targetId = ""; return; }
            order = o; t = 0; trip = 26 / speed(); delivered = false; targetId = o.getId();
            ctx.staffEvent(this, "Off to deliver " + o.summary());
        }
        t += dt;
        where = "ROAD";
        if (!delivered && t >= trip * 0.55) {
            delivered = true;
            ctx.riderDeliver(order, Math.min(0.95, 0.8 + 0.05 * (getLevel() - 1)));
        }
        activity = delivered ? "Riding back to the shop" : "Delivering " + order.summary();
        if (t >= trip) { order = null; where = "DELIVERY_HUB"; targetId = ""; }
    }
    public boolean isOut() { return order != null; }
}
