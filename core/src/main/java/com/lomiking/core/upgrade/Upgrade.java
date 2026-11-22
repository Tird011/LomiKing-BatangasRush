package com.lomiking.core.upgrade;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Something the player buys to grow the lomihan. Each upgrade has levels with rising costs and unlocks at a
 * player level; subclasses describe their own effect (abstraction + polymorphism). The engine asks
 * UpgradeShop.level(id) wherever an effect applies.
 */
public abstract class Upgrade {
    private final String id, name, description;
    private final int maxLevel, unlockLevel;
    private final double baseCost, costGrowth;

    protected Upgrade(String id, String name, String description, int maxLevel, int unlockLevel, double baseCost, double costGrowth) {
        this.id = id; this.name = name; this.description = description;
        this.maxLevel = maxLevel; this.unlockLevel = unlockLevel; this.baseCost = baseCost; this.costGrowth = costGrowth;
    }

    /** Cost to go from level-1 to level (level >= 1). */
    public double cost(int level) { return Math.round(baseCost * Math.pow(costGrowth, level - 1)); }
    /** What the given level does, in words for the shop card. */
    public abstract String effect(int level);

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getMaxLevel() { return maxLevel; }
    public int getUnlockLevel() { return unlockLevel; }

    public Map<String, Object> view(int current, int playerLevel, double money) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id); m.put("name", name); m.put("description", description);
        m.put("level", current); m.put("maxLevel", maxLevel); m.put("unlockLevel", unlockLevel);
        m.put("unlocked", playerLevel >= unlockLevel);
        boolean maxed = current >= maxLevel;
        m.put("maxed", maxed);
        m.put("nextCost", maxed ? 0 : cost(current + 1));
        m.put("canBuy", !maxed && playerLevel >= unlockLevel && money >= cost(current + 1));
        m.put("effectNow", current == 0 ? "Not bought yet" : effect(current));
        m.put("effectNext", maxed ? "" : effect(current + 1));
        return m;
    }
}
