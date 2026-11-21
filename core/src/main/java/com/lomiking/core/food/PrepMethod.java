package com.lomiking.core.food;

/** How an ingredient is handled in the kitchen; each maps to a touch gesture in the mini-games. */
public enum PrepMethod {
    CHOP("swipe to chop"), SLICE("swipe to slice"), MINCE("quick swipes to mince"),
    CRACK("tap to crack"), BOIL("tap when it's done"), FRY("tap to flip, tap to lift"),
    POUR("drag into the pot"), SPRINKLE("tap to shake"), MIX("stir in circles"),
    SQUEEZE("drag onto the bowl"), PLACE("drag onto the dish"), NONE("ready to use");

    private final String gestureHint;
    PrepMethod(String gestureHint) { this.gestureHint = gestureHint; }
    public String getGestureHint() { return gestureHint; }
}
