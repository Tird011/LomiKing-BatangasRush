package com.lomiking.core.progress;

import com.lomiking.core.economy.DayReport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Long-term growth: XP and levels, lifetime counters, daily goals, achievements and the "Sarap streak".
 * Pure bookkeeping: the engine feeds it what happened and turns the answers into events and rewards.
 */
public class Progression {
    private int level = 1, xp;
    private int totalServed, totalFiveStars, totalPerfect, totalDeliveries, bestStreak, streak, hires, tables = 2;
    private double totalEarned;
    private final Set<String> achieved = new LinkedHashSet<>();
    private final List<DailyGoal> goals = new ArrayList<>();

    public static final List<Achievement> ACHIEVEMENTS = Collections.unmodifiableList(Arrays.asList(
        new Achievement("first_bowl", "Unang Mangkok", "Serve your first customer", 50, p -> p.totalServed >= 1),
        new Achievement("suki_10", "May Suki Na", "Serve 10 customers", 100, p -> p.totalServed >= 10),
        new Achievement("suki_50", "Patok sa Barangay", "Serve 50 customers", 300, p -> p.totalServed >= 50),
        new Achievement("suki_200", "Lomi Legend", "Serve 200 customers", 1000, p -> p.totalServed >= 200),
        new Achievement("five_star", "Limang Bituin", "Get a 5-star rating", 80, p -> p.totalFiveStars >= 1),
        new Achievement("perfect_10", "Kamay ni Lola", "Cook 10 perfect dishes", 150, p -> p.totalPerfect >= 10),
        new Achievement("streak_5", "Sarap Streak", "Cook 5 perfect dishes in a row", 200, p -> p.bestStreak >= 5),
        new Achievement("delivery_1", "Rider Ready", "Complete a delivery", 100, p -> p.totalDeliveries >= 1),
        new Achievement("delivery_25", "Hari ng Kalsada", "Complete 25 deliveries", 400, p -> p.totalDeliveries >= 25),
        new Achievement("first_hire", "May Katulong Na", "Hire your first helper", 100, p -> p.hires >= 1),
        new Achievement("full_team", "Buong Pamilya", "Hire all 4 helpers", 800, p -> p.hires >= 4),
        new Achievement("six_tables", "Fiesta Ready", "Own 6 tables", 500, p -> p.tables >= 6),
        new Achievement("level_5", "Kilala Na", "Reach level 5", 200, p -> p.level >= 5),
        new Achievement("level_10", "LomiKing", "Reach level 10", 600, p -> p.level >= 10),
        new Achievement("earned_10k", "Pang-Negosyo", "Earn P10,000 in total", 500, p -> p.totalEarned >= 10000)));

    // ---------------- XP and levels ----------------
    /** @return the new levels reached (usually empty, sometimes one) */
    public List<Integer> addXp(int amount) {
        List<Integer> ups = new ArrayList<>();
        if (level >= Unlocks.MAX_LEVEL) return ups;
        xp += amount;
        while (level < Unlocks.MAX_LEVEL && xp >= Unlocks.xpToNext(level)) {
            xp -= Unlocks.xpToNext(level);
            level++;
            ups.add(level);
        }
        if (level >= Unlocks.MAX_LEVEL) xp = 0;
        return ups;
    }

    // ---------------- lifetime counters ----------------
    public void served(int stars, double earned) { totalServed++; totalEarned += earned; if (stars >= 5) totalFiveStars++; }
    public void delivered() { totalDeliveries++; }
    public void hired() { hires++; }
    public void setTables(int n) { tables = n; }

    /** A dish came off the stove. @return the new streak (0 if broken). */
    public int cooked(int cookStars) {
        if (cookStars >= 3) { totalPerfect++; streak++; bestStreak = Math.max(bestStreak, streak); }
        else streak = 0;
        return streak;
    }
    /** Tip bonus from the current streak: +10% per perfect dish in a row, up to +50%. */
    public double streakTipBonus() { return Math.min(0.5, 0.1 * Math.max(0, streak - 1)); }

    // ---------------- daily goals ----------------
    public void newGoals(Random rng, boolean deliveriesOpen) {
        goals.clear();
        int L = level;
        List<DailyGoal> pool = new ArrayList<>(Arrays.asList(
            new DailyGoal.Serve(3 + L, 40 + 15 * L, 25 + 5 * L),
            new DailyGoal.Earn(roundTo(150 + 70 * L, 10), 40 + 15 * L, 25 + 5 * L),
            new DailyGoal.FiveStars(1 + L / 3, 50 + 15 * L, 28 + 5 * L),
            new DailyGoal.Perfect(2 + L / 2, 50 + 15 * L, 28 + 5 * L),
            new DailyGoal.Chores(2 + L / 3, 30 + 10 * L, 20 + 4 * L)));
        if (deliveriesOpen) pool.add(new DailyGoal.Deliver(1 + L / 4, 60 + 15 * L, 30 + 5 * L));
        Collections.shuffle(pool, rng);
        goals.add(pool.remove(pool.indexOf(findServe(pool))));   // always one "serve" goal: the core loop
        goals.addAll(pool.subList(0, 2));
    }
    private static DailyGoal findServe(List<DailyGoal> pool) {
        for (DailyGoal g : pool) if (g instanceof DailyGoal.Serve) return g;
        return pool.get(0);
    }
    private static int roundTo(int v, int step) { return (v / step) * step; }

    /** @return goals that just completed */
    public List<DailyGoal> checkGoals(DayReport r) {
        List<DailyGoal> done = new ArrayList<>();
        for (DailyGoal g : goals) if (g.check(r)) done.add(g);
        return done;
    }
    public int goalsDone() { int n = 0; for (DailyGoal g : goals) if (g.isDone()) n++; return n; }

    // ---------------- achievements ----------------
    /** @return achievements that just unlocked */
    public List<Achievement> checkAchievements() {
        List<Achievement> out = new ArrayList<>();
        for (Achievement a : ACHIEVEMENTS) if (!achieved.contains(a.getId()) && a.isMet(this)) { achieved.add(a.getId()); out.add(a); }
        return out;
    }

    // ---------------- views + save ----------------
    public Map<String, Object> view() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("level", level); m.put("xp", xp); m.put("xpToNext", Unlocks.xpToNext(level)); m.put("maxLevel", Unlocks.MAX_LEVEL);
        m.put("streak", streak); m.put("streakBonus", streakTipBonus()); m.put("bestStreak", bestStreak);
        m.put("unlockedDishes", Unlocks.unlockedDishes(level));
        m.put("totalServed", totalServed); m.put("totalEarned", Math.round(totalEarned));
        List<String> next = new ArrayList<>(level < Unlocks.MAX_LEVEL ? Unlocks.textFor(level + 1) : Collections.<String>emptyList());
        m.put("nextUnlocks", next);
        return m;
    }
    public List<Object> goalsView(DayReport r) { List<Object> l = new ArrayList<>(); for (DailyGoal g : goals) l.add(g.view(r)); return l; }
    public List<Object> achievementsView() {
        List<Object> l = new ArrayList<>();
        for (Achievement a : ACHIEVEMENTS) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId()); m.put("title", a.getTitle()); m.put("description", a.getDescription());
            m.put("reward", a.getReward()); m.put("done", achieved.contains(a.getId()));
            l.add(m);
        }
        return l;
    }

    public Map<String, Object> save() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("level", level); m.put("xp", xp); m.put("totalServed", totalServed); m.put("totalFiveStars", totalFiveStars);
        m.put("totalPerfect", totalPerfect); m.put("totalDeliveries", totalDeliveries); m.put("bestStreak", bestStreak);
        m.put("hires", hires); m.put("tables", tables); m.put("totalEarned", totalEarned);
        m.put("achieved", new ArrayList<>(achieved));
        return m;
    }
    @SuppressWarnings("unchecked")
    public void load(Map<String, Object> m) {
        level = num(m, "level", 1); xp = num(m, "xp", 0); totalServed = num(m, "totalServed", 0);
        totalFiveStars = num(m, "totalFiveStars", 0); totalPerfect = num(m, "totalPerfect", 0);
        totalDeliveries = num(m, "totalDeliveries", 0); bestStreak = num(m, "bestStreak", 0);
        hires = num(m, "hires", 0); tables = num(m, "tables", 2);
        totalEarned = m.get("totalEarned") instanceof Number ? ((Number) m.get("totalEarned")).doubleValue() : 0;
        achieved.clear();
        if (m.get("achieved") instanceof List) for (Object o : (List<Object>) m.get("achieved")) achieved.add(String.valueOf(o));
    }
    private static int num(Map<String, Object> m, String k, int d) { return m.get(k) instanceof Number ? ((Number) m.get(k)).intValue() : d; }

    public int getLevel() { return level; }
    public void setLevel(int l) { level = Math.max(1, Math.min(Unlocks.MAX_LEVEL, l)); }
    public int getXp() { return xp; }
    public int getStreak() { return streak; }
    public int getTotalServed() { return totalServed; }
    public List<DailyGoal> getGoals() { return Collections.unmodifiableList(goals); }
}
