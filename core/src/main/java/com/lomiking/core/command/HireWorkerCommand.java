package com.lomiking.core.command;

import com.lomiking.core.engine.GameEngine;

/** Hire a helper. */
public final class HireWorkerCommand implements GameCommand {
    private final String id;
    public HireWorkerCommand(String id) { this.id = id; }
    @Override public CommandResult execute(GameEngine e) { return e.hireWorker(id); }
    @Override public String name() { return "hire"; }
}
