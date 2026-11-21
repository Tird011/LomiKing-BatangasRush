package com.lomiking.core.cooking;

/** Touch / mic gestures the view layer turns into PlayerInput. */
public enum InputType {
    TAP,        // single tap
    SWIPE,      // value = swipe length (0..1 of screen), dx/dy = direction
    CIRCLE,     // value = rotations made since the last CIRCLE input
    HOLD,       // value = seconds held
    MIC_LEVEL,  // value = microphone loudness 0..1 (blowing / fanning)
    DRAG_DROP,  // itemId = what was dropped onto the pot/plate
    CONFIRM     // "done" button
}
