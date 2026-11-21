package com.lomiking.core.engine;

/** Tunable numbers in one place, so balancing doesn't mean hunting through code. */
public class GameConfig {
    public long seed = 7;
    public double dayLengthSeconds = 240;
    public double startingMoney = 500;
    public int startingStock = 8;
    public int tableCount = 4;
    public int maxQueue = 4;
    public double spawnMinSeconds = 10, spawnMaxSeconds = 18;
    public double firstCustomerDelay = 2;
    public double deliveryChance = 0.2;
    public int lowStockThreshold = 2;
    public int restockTarget = 8;
    public double deliveryDistance = 300;
    public int dishPileForTask = 3;
    public double floorDirtForTask = 0.4;
    public double startingReputation = 3;

    public static GameConfig defaults() { return new GameConfig(); }
    public GameConfig seed(long s) { seed = s; return this; }
    public GameConfig dayLength(double s) { dayLengthSeconds = s; return this; }
    public GameConfig deliveryChance(double c) { deliveryChance = c; return this; }
    public GameConfig spawnInterval(double min, double max) { spawnMinSeconds = min; spawnMaxSeconds = max; return this; }
}
