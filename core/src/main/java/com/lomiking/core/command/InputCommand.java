package com.lomiking.core.command;

import com.lomiking.core.cooking.PlayerInput;
import com.lomiking.core.engine.GameEngine;

/** A touch or mic gesture for the current mini-game. */
public final class InputCommand implements GameCommand {
    private final PlayerInput input;
    public InputCommand(PlayerInput input) { this.input = input; }
    @Override public CommandResult execute(GameEngine e) { return e.input(input); }
    @Override public String name() { return "input"; }
    public PlayerInput getInput() { return input; }
}
