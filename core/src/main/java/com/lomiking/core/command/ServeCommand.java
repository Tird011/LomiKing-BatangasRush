package com.lomiking.core.command;

import com.lomiking.core.engine.GameEngine;

/** Serve a ready order at the counter. */
public final class ServeCommand implements GameCommand {
    private final String orderId;   // null = first ready order
    public ServeCommand(String orderId) { this.orderId = orderId; }
    @Override public CommandResult execute(GameEngine e) { return e.serve(orderId); }
    @Override public String name() { return "serve"; }
}
