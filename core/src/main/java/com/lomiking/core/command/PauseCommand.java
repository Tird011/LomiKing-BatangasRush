package com.lomiking.core.command;

import com.lomiking.core.engine.GameEngine;

/** Pause the game. */
public final class PauseCommand implements GameCommand {
    @Override public CommandResult execute(GameEngine e) { return e.pause(); }
    @Override public String name() { return "pause"; }
}
