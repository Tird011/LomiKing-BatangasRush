package com.lomiking.core.engine;

/** Tunable numbers in one place, so balancing doesn't mean hunting through code. */
public class GameConfig {
    public long seed = 7;
    public double dayLengthSeconds = 240;
    public double startingMoney = 300;
    public int startingStock = 8;
    public int tableCount = 2;              // the diner starts small; the Extra Table upgrade adds more
    public int maxQueue = 5;
    public double spawnMinSeconds = 14, spawnMaxSeconds = 22;
    /** Cozy game: everyone waits longer than in a rush game. */
    public double patienceMultiplier = 1.6;
    /** Start at a higher level (sandbox / tests): unlocks everything up to it. */
    public int startLevel = 1;
    /** Morning supplier restock (turn off to test running out of stock). */
    public boolean morningSupplier = true;
    public double firstCustomerDelay = 2;
    public double deliveryChance = 0.2;
    public int lowStockThreshold = 2;
    public int restockTarget = 8;
    public double deliveryDistance = 300;
    public int dishPileForTask = 4;
    public double floorDirtForTask = 0.55;
    public double startingReputation = 3;

    public static GameConfig defaults() { return new GameConfig(); }
    public GameConfig seed(long s) { seed = s; return this; }
    public GameConfig dayLength(double s) { dayLengthSeconds = s; return this; }
    public GameConfig deliveryChance(double c) { deliveryChance = c; return this; }
    public GameConfig spawnInterval(double min, double max) { spawnMinSeconds = min; spawnMaxSeconds = max; return this; }
    public GameConfig startLevel(int l) { startLevel = l; return this; }
    public GameConfig tables(int n) { tableCount = n; return this; }
}
