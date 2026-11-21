package com.lomiking.core.customer;

import com.lomiking.core.cooking.CookedDish;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A ticket hanging in the kitchen. */
public class Order {
    private final String id;
    private final String customerId;
    private final boolean delivery;
    private final List<OrderLine> lines;
    private final double createdAt;
    private OrderStatus status = OrderStatus.PENDING;

    public Order(String id, String customerId, boolean delivery, List<OrderLine> lines, double createdAt) {
        if (lines.isEmpty()) throw new IllegalArgumentException("Order needs at least one dish");
        this.id = id; this.customerId = customerId; this.delivery = delivery;
        this.lines = Collections.unmodifiableList(new ArrayList<>(lines)); this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public boolean isDelivery() { return delivery; }
    public List<OrderLine> getLines() { return lines; }
    public OrderStatus getStatus() { return status; }
    public double getCreatedAt() { return createdAt; }

    /** Index of the next dish nobody is cooking yet, or -1. */
    public int nextLineToCook() {
        for (int i = 0; i < lines.size(); i++) if (!lines.get(i).isCooked() && !lines.get(i).isCooking()) return i;
        return -1;
    }
    public boolean allCooked() {
        for (OrderLine l : lines) if (!l.isCooked()) return false;
        return true;
    }
    public void startCooking(int lineIndex) {
        requireOpen();
        lines.get(lineIndex).setCooking(true);
        status = OrderStatus.COOKING;
    }
    public void cancelCooking(int lineIndex) { lines.get(lineIndex).setCooking(false); }
    public void finishCooking(int lineIndex, CookedDish dish) {
        lines.get(lineIndex).setCooked(dish);
        if (allCooked()) status = OrderStatus.READY;
    }
    public void markOutForDelivery() { requireStatus(OrderStatus.READY); status = OrderStatus.OUT_FOR_DELIVERY; }
    public void markServed() { requireStatus(OrderStatus.READY); status = OrderStatus.SERVED; }
    public void markDelivered() { requireStatus(OrderStatus.OUT_FOR_DELIVERY); status = OrderStatus.DELIVERED; }
    public void backToReady() { requireStatus(OrderStatus.OUT_FOR_DELIVERY); status = OrderStatus.READY; }
    public void cancel() { status = OrderStatus.CANCELLED; }

    public boolean isOpen() { return status != OrderStatus.SERVED && status != OrderStatus.DELIVERED && status != OrderStatus.CANCELLED; }

    public double totalPrice() {
        double t = 0;
        for (OrderLine l : lines) t += l.getDish().getPrice();
        return t;
    }
    public double averageQuality() {
        double q = 0; int n = 0;
        for (OrderLine l : lines) { CookedDish c = l.getCooked(); if (c != null) { q += c.getQuality(); n++; } }
        return n == 0 ? 0 : q / n;
    }
    public double maxFragility() {
        double f = 0;
        for (OrderLine l : lines) f = Math.max(f, l.getDish().getDeliveryFragility());
        return f;
    }
    public String summary() {
        StringBuilder sb = new StringBuilder();
        for (OrderLine l : lines) {
            if (sb.length() > 0) sb.append(" + ");
            sb.append(l.getDish().getName());
            if (!l.getExtras().isEmpty()) sb.append(" (extra ").append(String.join(", ", l.getExtras())).append(")");
        }
        return sb.toString();
    }

    private void requireOpen() { if (!isOpen()) throw new IllegalStateException("Order " + id + " is " + status); }
    private void requireStatus(OrderStatus s) { if (status != s) throw new IllegalStateException("Order " + id + " is " + status + ", expected " + s); }
}
