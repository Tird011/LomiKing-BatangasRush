package com.lomiking.core.engine;

import com.lomiking.core.character.Emote;
import com.lomiking.core.economy.Inventory;
import com.lomiking.core.economy.Wallet;
import com.lomiking.core.event.EventType;
import com.lomiking.core.shop.Shop;
import java.util.Random;

/**
 * The services game objects (tasks, scenes) may use. GameEngine implements it; tests can fake it.
 * Keeps objects from depending on the whole engine (abstraction / loose coupling).
 */
public interface GameContext {
    double time();
    Random random();
    Shop shop();
    Inventory inventory();
    Wallet wallet();
    void emit(EventType type, Object... keyValues);
    void react(Emote emote, double seconds);
}
