package com.lomiking.core.task;

import com.lomiking.core.customer.Order;
import com.lomiking.core.customer.OrderStatus;
import com.lomiking.core.engine.GameContext;
import java.util.Map;

/** A delivery note: done when the order arrives at the customer's door. */
public class DeliveryTask extends Task {
    private final Order order;
    private final String address;

    public DeliveryTask(String id, Order order, String address) {
        super(id, "Deliver to " + address, Sector.DELIVERY_HUB, 10, -1);
        this.order = order; this.address = address;
    }
    @Override public boolean isInteractive() { return false; }
    @Override public double getProgress() {
        switch (order.getStatus()) {
            case DELIVERED: return 1;
            case OUT_FOR_DELIVERY: return 0.75;
            case READY: return 0.5;
            case COOKING: return 0.25;
            default: return 0;
        }
    }
    @Override protected boolean isGoalMet(GameContext ctx) {
        if (order.getStatus() == OrderStatus.CANCELLED) { expire(); return false; }
        return order.getStatus() == OrderStatus.DELIVERED;
    }
    public Order getOrder() { return order; }
    @Override protected void fillViewState(Map<String, Object> m) { m.put("orderId", order.getId()); m.put("address", address); }
}
