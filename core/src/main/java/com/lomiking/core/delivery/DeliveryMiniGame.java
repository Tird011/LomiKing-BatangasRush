package com.lomiking.core.delivery;

import com.lomiking.core.cooking.InputType;
import com.lomiking.core.cooking.PlayerInput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Motorbike delivery run: 3 lanes, swipe left/right to dodge. Bumps spill the food (soups spill most),
 * and the food cools the longer it takes. Quality on arrival = condition and warmth.
 */
public class DeliveryMiniGame {
    public static final int LANES = 3;
    public static final double SPEED = 15;         // m/s
    public static final double BOOST_SPEED = 22;   // while holding the throttle
    private final String orderId;
    private final double length;
    private final double fragility;
    private final List<Obstacle> obstacles = new ArrayList<>();
    private final List<Obstacle> newHits = new ArrayList<>();
    private int lane = 1;
    private double distance, elapsed, condition = 1, warmth = 1, boostTime;
    private boolean finished;

    private double speedMult = 1, spillMult = 1;

    public DeliveryMiniGame(String orderId, double length, double fragility, Random rng, double speedMult, double spillMult) {
        this(orderId, length, fragility, rng);
        this.speedMult = speedMult; this.spillMult = spillMult;
    }

    public DeliveryMiniGame(String orderId, double length, double fragility, Random rng) {
        this.orderId = orderId; this.length = length; this.fragility = fragility;
        ObstacleType[] types = ObstacleType.values();
        for (double pos = 40; pos < length - 20; pos += 25 + rng.nextInt(25))
            obstacles.add(new Obstacle(types[rng.nextInt(types.length)], rng.nextInt(LANES), pos));
    }

    public void handleInput(PlayerInput in) {
        if (finished) return;
        if (in.getType() == InputType.SWIPE) {
            if (in.getDx() < 0) lane = Math.max(0, lane - 1);
            else if (in.getDx() > 0) lane = Math.min(LANES - 1, lane + 1);
        } else if (in.getType() == InputType.HOLD) {
            boostTime = Math.max(boostTime, in.getValue());   // risky: faster, but bumps hurt more
        }
    }

    public void update(double dt) {
        if (finished) return;
        elapsed += dt;
        boolean boosting = boostTime > 0;
        boostTime = Math.max(0, boostTime - dt);
        distance += (boosting ? BOOST_SPEED : SPEED) * speedMult * dt;
        warmth = Math.max(0, warmth - dt * 0.012 * (1 + fragility));
        for (Obstacle o : obstacles) {
            if (!o.isPassed() && distance >= o.getPosition()) {
                boolean hit = o.getLane() == lane;
                o.resolve(hit);
                if (hit) {
                    condition = Math.max(0, condition - 0.25 * (0.5 + fragility) * (boosting ? 1.5 : 1) * spillMult);
                    newHits.add(o);
                }
            }
        }
        if (distance >= length || condition <= 0) finished = true;
    }

    /** Hits since the last call (for bump animations / sounds). */
    public List<Obstacle> takeNewHits() {
        List<Obstacle> h = new ArrayList<>(newHits);
        newHits.clear();
        return h;
    }

    public boolean isFinished() { return finished; }
    public boolean isSuccess() { return finished && condition > 0; }
    public double getQuality() { return Math.max(0, Math.min(1, condition * 0.7 + warmth * 0.3)); }
    public String getOrderId() { return orderId; }
    public int getLane() { return lane; }
    public double getDistance() { return distance; }
    public double getLength() { return length; }
    public double getCondition() { return condition; }
    public double getWarmth() { return warmth; }
    public List<Obstacle> getObstacles() { return Collections.unmodifiableList(obstacles); }

    public Map<String, Object> viewState() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("orderId", orderId); m.put("lane", lane); m.put("distance", Math.round(distance * 10) / 10.0);
        m.put("length", length); m.put("condition", Math.round(condition * 100) / 100.0);
        m.put("warmth", Math.round(warmth * 100) / 100.0); m.put("finished", finished);
        return m;
    }
}
