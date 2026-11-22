package com.lomiking.core.staff;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A helper Mang Kaloy can hire with his earnings. Shared: hiring, training, wages, what they're doing now.
 * Each subclass implements its own job in work() (polymorphism), using only the StaffContext operations.
 */
public abstract class Worker {
    public static final int MAX_LEVEL = 3;
    private final String id, name, modelId;
    private final StaffRole role;
    private final double hireCost, wage;
    private final int unlockLevel;
    private int level;                 // 0 = not hired
    private boolean resting;
    protected String activity = "Waiting for work";
    protected String where = "COUNTER";
    protected String targetId = "";

    protected Worker(String id, String name, StaffRole role, String modelId, double hireCost, double wage, int unlockLevel) {
        this.id = id; this.name = name; this.role = role; this.modelId = modelId;
        this.hireCost = hireCost; this.wage = wage; this.unlockLevel = unlockLevel;
    }

    /** Does one slice of the job. Called every tick while hired and not resting. */
    protected abstract void work(double dt, StaffContext ctx);
    public abstract String jobDescription();

    public final void update(double dt, StaffContext ctx) {
        if (!isHired()) return;
        if (resting) { activity = "Resting"; return; }
        work(dt, ctx);
    }

    /** Each training level makes them ~35% faster. */
    protected double speed() { return 1 + 0.35 * (level - 1); }

    public boolean isHired() { return level > 0; }
    public void hire() { if (level == 0) level = 1; }
    public void train() { if (level > 0 && level < MAX_LEVEL) level++; }
    public void setLevel(int l) { level = Math.max(0, Math.min(MAX_LEVEL, l)); }
    public void setResting(boolean r) { resting = r; }
    public double trainCost() { return Math.round(hireCost * 0.9 * level); }
    public double dailyWage() { return isHired() ? Math.round(wage * (1 + 0.25 * (level - 1))) : 0; }

    public String getId() { return id; }
    public String getName() { return name; }
    public StaffRole getRole() { return role; }
    public String getModelId() { return modelId; }
    public double getHireCost() { return hireCost; }
    public int getUnlockLevel() { return unlockLevel; }
    public int getLevel() { return level; }
    public boolean isResting() { return resting; }
    public String getActivity() { return activity; }

    public Map<String, Object> view(int playerLevel, double money) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id); m.put("name", name); m.put("role", role.name()); m.put("model", modelId);
        m.put("job", jobDescription()); m.put("level", level); m.put("maxLevel", MAX_LEVEL); m.put("hired", isHired());
        m.put("unlockLevel", unlockLevel); m.put("unlocked", playerLevel >= unlockLevel);
        m.put("hireCost", hireCost); m.put("trainCost", isHired() && level < MAX_LEVEL ? trainCost() : 0);
        m.put("wage", isHired() ? dailyWage() : Math.round(wage));
        m.put("canHire", !isHired() && playerLevel >= unlockLevel && money >= hireCost);
        m.put("canTrain", isHired() && level < MAX_LEVEL && money >= trainCost());
        m.put("resting", resting); m.put("activity", activity); m.put("where", where); m.put("target", targetId);
        return m;
    }
}
