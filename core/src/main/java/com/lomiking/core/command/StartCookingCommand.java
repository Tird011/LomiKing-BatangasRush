package com.lomiking.core.command;

import com.lomiking.core.engine.GameEngine;

/** Start cooking the next dish of an order. */
public final class StartCookingCommand implements GameCommand {
    private final String orderId;   // null = oldest waiting order
    public StartCookingCommand(String orderId) { this.orderId = orderId; }
    @Override public CommandResult execute(GameEngine e) { return e.startCooking(orderId); }
    @Override public String name() { return "cook"; }
}
