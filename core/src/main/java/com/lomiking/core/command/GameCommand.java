package com.lomiking.core.command;

import com.lomiking.core.engine.GameEngine;

/**
 * One player action as an object (command pattern). The UI / React Native side builds these
 * (usually from JSON via CommandParser) and the engine runs them.
 */
public interface GameCommand {
    CommandResult execute(GameEngine engine);
    String name();
}
