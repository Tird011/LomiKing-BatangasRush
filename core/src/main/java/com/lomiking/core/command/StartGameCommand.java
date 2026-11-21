package com.lomiking.core.command;

import com.lomiking.core.engine.GameEngine;

/** Start button on the menu. */
public final class StartGameCommand implements GameCommand {
    @Override public CommandResult execute(GameEngine e) { return e.startGame(); }
    @Override public String name() { return "start"; }
}
