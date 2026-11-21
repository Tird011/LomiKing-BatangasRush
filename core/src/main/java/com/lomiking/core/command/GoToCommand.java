package com.lomiking.core.command;

import com.lomiking.core.scene.SceneId;
import com.lomiking.core.engine.GameEngine;

/** Walk to another room. */
public final class GoToCommand implements GameCommand {
    private final SceneId target;
    public GoToCommand(SceneId target) { this.target = target; }
    @Override public CommandResult execute(GameEngine e) { return e.goTo(target); }
    @Override public String name() { return "goTo"; }
}
