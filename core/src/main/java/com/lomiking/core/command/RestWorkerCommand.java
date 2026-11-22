package com.lomiking.core.command;

import com.lomiking.core.engine.GameEngine;

/** Let a helper rest (so you can do the job yourself) or call them back. */
public final class RestWorkerCommand implements GameCommand {
    private final String id;
    private final boolean resting;
    public RestWorkerCommand(String id, boolean resting) { this.id = id; this.resting = resting; }
    @Override public CommandResult execute(GameEngine e) { return e.setWorkerResting(id, resting); }
    @Override public String name() { return "rest"; }
}
