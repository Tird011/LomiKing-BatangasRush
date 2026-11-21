package com.lomiking.core.command;

import com.lomiking.core.engine.GameEngine;

/** Continue after the end-of-day summary. */
public final class NextDayCommand implements GameCommand {
    @Override public CommandResult execute(GameEngine e) { return e.nextDay(); }
    @Override public String name() { return "nextDay"; }
}
