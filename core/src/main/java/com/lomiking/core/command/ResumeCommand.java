package com.lomiking.core.command;

import com.lomiking.core.engine.GameEngine;

/** Resume after pause. */
public final class ResumeCommand implements GameCommand {
    @Override public CommandResult execute(GameEngine e) { return e.resume(); }
    @Override public String name() { return "resume"; }
}
