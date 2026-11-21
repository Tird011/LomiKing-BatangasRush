package com.lomiking.core.cooking;

/** One input from the player. Immutable; create with the static helpers. */
public final class PlayerInput {
    private final InputType type;
    private final double value;
    private final String itemId;
    private final double dx, dy;

    public PlayerInput(InputType type, double value, String itemId, double dx, double dy) {
        this.type = type; this.value = value; this.itemId = itemId; this.dx = dx; this.dy = dy;
    }
    public static PlayerInput tap() { return new PlayerInput(InputType.TAP, 0, null, 0, 0); }
    public static PlayerInput confirm() { return new PlayerInput(InputType.CONFIRM, 0, null, 0, 0); }
    public static PlayerInput swipe(double length, double dx, double dy) { return new PlayerInput(InputType.SWIPE, length, null, dx, dy); }
    public static PlayerInput circle(double rotations) { return new PlayerInput(InputType.CIRCLE, rotations, null, 0, 0); }
    public static PlayerInput mic(double level) { return new PlayerInput(InputType.MIC_LEVEL, level, null, 0, 0); }
    public static PlayerInput hold(double seconds) { return new PlayerInput(InputType.HOLD, seconds, null, 0, 0); }
    public static PlayerInput drop(String itemId) { return new PlayerInput(InputType.DRAG_DROP, 0, itemId, 0, 0); }

    public InputType getType() { return type; }
    public double getValue() { return value; }
    public String getItemId() { return itemId; }
    public double getDx() { return dx; }
    public double getDy() { return dy; }
    @Override public String toString() { return type + (itemId != null ? "(" + itemId + ")" : "(" + value + ")"); }
}
