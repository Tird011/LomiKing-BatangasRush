package com.lomiking.core.staff;

import com.lomiking.core.customer.Customer;
import com.lomiking.core.customer.Order;

/** Ate Joy: takes orders at the counter and brings ready food to the tables. */
public class ServerWorker extends Worker {
    private double cooldown = 2;
    public ServerWorker() { super("server", "Ate Joy", StaffRole.SERVER, "worker_server", 300, 60, 3); }
    @Override public String jobDescription() { return "Takes orders and serves food"; }

    @Override protected void work(double dt, StaffContext ctx) {
        cooldown -= dt;
        if (cooldown > 0) return;
        Order ready = ctx.readyDineInOrder();
        if (ready != null && ctx.staffServe(ready)) {
            activity = "Serving " + ready.summary(); where = "MAIN_DINER"; targetId = ready.getId();
            ctx.staffEvent(this, activity);
            cooldown = 5 / speed();
            return;
        }
        Customer c = ctx.customerWaitingToOrder();
        if (c != null && ctx.staffTakeOrder(c)) {
            activity = "Taking " + c.getName() + "'s order"; where = "COUNTER"; targetId = c.getId();
            ctx.staffEvent(this, activity);
            cooldown = 6 / speed();
            return;
        }
        activity = "Waiting at the counter"; where = "COUNTER"; targetId = "";
        cooldown = 1;
    }
}
