package com.lomiking.core.command;

import com.lomiking.core.engine.GameEngine;

/** Pick a chore note to work on. */
public final class StartTaskCommand implements GameCommand {
    private final String taskId;
    public StartTaskCommand(String taskId) { this.taskId = taskId; }
    @Override public CommandResult execute(GameEngine e) { return e.startTask(taskId); }
    @Override public String name() { return "startTask"; }
}
