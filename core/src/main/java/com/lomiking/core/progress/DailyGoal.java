package com.lomiking.core.progress;

import com.lomiking.core.economy.DayReport;
import java.util.LinkedHashMap;
import java.util.Map;

/** One of today's three goals. Subclasses decide what they count; rewards are paid the moment it completes. */
public abstract class DailyGoal {
    private final String id;
    protected final int target;
    private final double rewardMoney;
    private final int rewardXp;
    private boolean done;

    protected DailyGoal(String id, int target, double rewardMoney, int rewardXp) {
        this.id = id; this.target = Math.max(1, target); this.rewardMoney = rewardMoney; this.rewardXp = rewardXp;
    }

    public abstract String title();
    protected abstract double current(DayReport r);

    /** @return true the moment it becomes complete */
    public boolean check(DayReport r) {
        if (done) return false;
        if (current(r) >= target) { done = true; return true; }
        return false;
    }

    public boolean isDone() { return done; }
    public double getRewardMoney() { return rewardMoney; }
    public int getRewardXp() { return rewardXp; }
    public String getId() { return id; }

    public Map<String, Object> view(DayReport r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id); m.put("title", title()); m.put("progress", Math.min(target, Math.floor(current(r))));
        m.put("target", target); m.put("done", done); m.put("rewardMoney", rewardMoney); m.put("rewardXp", rewardXp);
        return m;
    }

    // ---- the goal types ----
    public static final class Serve extends DailyGoal {
        public Serve(int n, double money, int xp) { super("serve", n, money, xp); }
        public String title() { return "Serve " + target + " customers"; }
        protected double current(DayReport r) { return r.getCustomersServed() + r.getDeliveriesDone(); }
    }
    public static final class Earn extends DailyGoal {
        public Earn(int pesos, double money, int xp) { super("earn", pesos, money, xp); }
        public String title() { return "Earn P" + target + " in sales"; }
        protected double current(DayReport r) { return r.getEarned(); }
    }
    public static final class FiveStars extends DailyGoal {
        public FiveStars(int n, double money, int xp) { super("fivestars", n, money, xp); }
        public String title() { return "Get " + target + " five-star rating" + (target > 1 ? "s" : ""); }
        protected double current(DayReport r) { return r.getFiveStars(); }
    }
    public static final class Perfect extends DailyGoal {
        public Perfect(int n, double money, int xp) { super("perfect", n, money, xp); }
        public String title() { return "Cook " + target + " perfect dish" + (target > 1 ? "es" : ""); }
        protected double current(DayReport r) { return r.getPerfectDishes(); }
    }
    public static final class Deliver extends DailyGoal {
        public Deliver(int n, double money, int xp) { super("deliver", n, money, xp); }
        public String title() { return "Deliver " + target + " order" + (target > 1 ? "s" : ""); }
        protected double current(DayReport r) { return r.getDeliveriesDone(); }
    }
    public static final class Chores extends DailyGoal {
        public Chores(int n, double money, int xp) { super("chores", n, money, xp); }
        public String title() { return "Finish " + target + " chores"; }
        protected double current(DayReport r) { return r.getChores(); }
    }
}
