package com.lomiking.core.progress;

import java.util.function.Predicate;

/** A lifetime milestone in the collection book. */
public final class Achievement {
    private final String id, title, description;
    private final double reward;
    private final Predicate<Progression> test;

    public Achievement(String id, String title, String description, double reward, Predicate<Progression> test) {
        this.id = id; this.title = title; this.description = description; this.reward = reward; this.test = test;
    }
    public boolean isMet(Progression p) { return test.test(p); }
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public double getReward() { return reward; }
}
