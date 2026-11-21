package com.lomiking.core.command;

import com.lomiking.core.engine.GameEngine;

/** Take a customer's order at the counter. */
public final class TakeOrderCommand implements GameCommand {
    private final String customerId;   // null = first in line
    public TakeOrderCommand(String customerId) { this.customerId = customerId; }
    @Override public CommandResult execute(GameEngine e) { return e.takeOrder(customerId); }
    @Override public String name() { return "takeOrder"; }
}
