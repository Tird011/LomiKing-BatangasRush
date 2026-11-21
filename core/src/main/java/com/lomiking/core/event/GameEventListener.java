package com.lomiking.core.event;

/** Implemented by anything that reacts to game events (renderer, audio, tests, the JSON bridge). */
public interface GameEventListener {
    void onEvent(GameEvent event);
}
