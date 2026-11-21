package com.lomiking.core.delivery;

public final class Obstacle {
    private final ObstacleType type;
    private final int lane;
    private final double position;   // meters from the shop
    private boolean passed, hit;

    public Obstacle(ObstacleType type, int lane, double position) { this.type = type; this.lane = lane; this.position = position; }
    public ObstacleType getType() { return type; }
    public int getLane() { return lane; }
    public double getPosition() { return position; }
    public boolean isPassed() { return passed; }
    public boolean isHit() { return hit; }
    void resolve(boolean wasHit) { passed = true; hit = wasHit; }
}
