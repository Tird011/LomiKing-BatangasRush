package com.lomiking.core.customer;

import com.lomiking.core.food.Dish;
import com.lomiking.core.food.Menu;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Base customer. The visit flow (arrive, queue, order, wait, eat, pay, leave) is shared; each customer
 * type overrides what they order, how patient they are, what they care about and how they tip.
 */
public abstract class Customer {
    private final String id;
    private final String name;
    private CustomerAction action = CustomerAction.ARRIVING;
    private double patience;
    private final double maxPatience;
    private double arriveTimer = 1.5, eatTimer;
    private Order order;
    private int tableId = -1;
    private double servedQuality = -1, speedScore;
    private Rating rating;
    private Payment payment;

    protected Customer(String id, String name) {
        this.id = id; this.name = name;
        this.maxPatience = basePatience();
        this.patience = maxPatience;
    }

    // ---- what each type decides (polymorphism) ----
    public abstract String getTypeLabel();
    /** Id of the 3D model / sprite for the renderer. */
    public abstract String getModelId();
    /** Seconds of patience while queuing and waiting. */
    protected abstract double basePatience();
    protected abstract List<Dish> chooseDishes(Menu menu, Random rng);
    /** Tip as a fraction of the bill for a given star rating. */
    protected abstract double tipRate(int stars);
    /** How much food quality matters vs speed (0..1). */
    protected double qualityWeight() { return 0.7; }
    protected double eatSeconds() { return 6; }
    protected double chanceOfExtra() { return 0.4; }
    public String greeting() { return "Isang order nga po!"; }
    protected String comment(int stars) {
        switch (stars) {
            case 5: return "Sarap! Babalik ako!";
            case 4: return "Masarap, salamat po.";
            case 3: return "Okay lang.";
            case 2: return "Medyo matagal at matabang...";
            default: return "Hindi na ako babalik.";
        }
    }
    /** Remote customers (delivery) don't stand in line. */
    public boolean isRemote() { return false; }

    // ---- shared behavior (template methods) ----

    /** Advances timers. The engine compares action/emotion before and after to fire events. */
    public void update(double dt) {
        switch (action) {
            case ARRIVING:
                arriveTimer -= dt;
                if (arriveTimer <= 0) action = CustomerAction.IN_LINE;
                break;
            case IN_LINE:
                drainPatience(dt);
                break;
            case WAITING_FOR_FOOD:
                drainPatience(dt * 0.7);   // sitting down is a bit more bearable
                break;
            case EATING:
                eatTimer -= dt;
                if (eatTimer <= 0) action = CustomerAction.PAYING;
                break;
            default:
                break;
        }
    }

    private void drainPatience(double amount) {
        patience = Math.max(0, patience - amount);
        if (patience <= 0) action = CustomerAction.LEFT_ANGRY;
    }

    public Order placeOrder(Menu menu, Random rng, String orderId, double now) {
        if (action != CustomerAction.IN_LINE && !(isRemote() && action == CustomerAction.ARRIVING))
            throw new IllegalStateException(name + " is not ready to order (" + action + ")");
        List<OrderLine> lines = new ArrayList<>();
        for (Dish d : chooseDishes(menu, rng)) {
            List<String> extras = new ArrayList<>();
            List<String> possible = d.getPossibleExtras();
            if (!possible.isEmpty() && rng.nextDouble() < chanceOfExtra()) extras.add(possible.get(rng.nextInt(possible.size())));
            lines.add(new OrderLine(d, extras));
        }
        order = new Order(orderId, id, isRemote(), lines, now);
        action = CustomerAction.WAITING_FOR_FOOD;
        return order;
    }

    public void seatAt(int tableId) { this.tableId = tableId; }

    /** Food arrives (served at the table or delivered). */
    public void receiveFood(double quality) {
        servedQuality = quality;
        speedScore = patience / maxPatience;
        eatTimer = eatSeconds();
        action = CustomerAction.EATING;
    }

    public Rating rate() {
        double w = qualityWeight();
        double score = servedQuality * w + speedScore * (1 - w);
        int stars = (int) Math.round(1 + 4 * Math.max(0, Math.min(1, score)));
        rating = new Rating(stars, comment(stars));
        return rating;
    }

    public Payment pay() {
        if (rating == null) rate();
        double price = order.totalPrice();
        double tip = Math.round(price * tipRate(rating.getStars()));
        payment = new Payment(price, tip);
        action = CustomerAction.LEFT;
        return payment;
    }

    public void leaveAngry() { action = CustomerAction.LEFT_ANGRY; }

    public Emotion getEmotion() {
        if (action == CustomerAction.LEFT_ANGRY) return Emotion.ANGRY;
        if (action == CustomerAction.EATING || action == CustomerAction.PAYING || action == CustomerAction.LEFT) {
            if (rating != null) return rating.getStars() >= 5 ? Emotion.DELIGHTED : rating.getStars() >= 3 ? Emotion.HAPPY : Emotion.NEUTRAL;
            return servedQuality >= 0.8 ? Emotion.DELIGHTED : servedQuality >= 0.5 ? Emotion.HAPPY : Emotion.NEUTRAL;
        }
        double r = patience / maxPatience;
        return r > 0.6 ? Emotion.HAPPY : r > 0.35 ? Emotion.NEUTRAL : r > 0.15 ? Emotion.IMPATIENT : Emotion.ANGRY;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public CustomerAction getAction() { return action; }
    public double getPatience() { return patience; }
    public double getPatienceRatio() { return patience / maxPatience; }
    public Order getOrder() { return order; }
    public int getTableId() { return tableId; }
    public Rating getRating() { return rating; }
    public Payment getPayment() { return payment; }

    /** Small helper for subclasses. */
    protected static <T> T pick(List<T> list, Random rng) { return list.get(rng.nextInt(list.size())); }
}
