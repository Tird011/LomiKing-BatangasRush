package com.lomiking.core.command;

import com.lomiking.core.engine.GameEngine;

/** Train a helper to work faster. */
public final class TrainWorkerCommand implements GameCommand {
    private final String id;
    public TrainWorkerCommand(String id) { this.id = id; }
    @Override public CommandResult execute(GameEngine e) { return e.trainWorker(id); }
    @Override public String name() { return "train"; }
}
