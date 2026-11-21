package com.lomiking.core.task;

import com.lomiking.core.customer.Order;
import com.lomiking.core.customer.OrderStatus;
import com.lomiking.core.engine.GameContext;
import java.util.Map;

/** The order ticket pinned in the kitchen. Done once the order reaches the customer. */
public class CookOrderTask extends Task {
    private final Order order;
    public CookOrderTask(String id, Order order) {
        super(id, "Cook: " + order.summary(), Sector.KITCHEN, 0, -1);
        this.order = order;
    }
    @Override public boolean isInteractive() { return false; }
    @Override public double getProgress() {
        int cooked = 0;
        for (int i = 0; i < order.getLines().size(); i++) if (order.getLines().get(i).isCooked()) cooked++;
        return (double) cooked / order.getLines().size();
    }
    @Override protected boolean isGoalMet(GameContext ctx) {
        if (order.getStatus() == OrderStatus.CANCELLED) { expire(); return false; }
        return order.getStatus() == OrderStatus.SERVED || order.getStatus() == OrderStatus.DELIVERED;
    }
    public Order getOrder() { return order; }
    @Override protected void fillViewState(Map<String, Object> m) {
        m.put("orderId", order.getId()); m.put("orderStatus", order.getStatus().name()); m.put("delivery", order.isDelivery());
    }
}
