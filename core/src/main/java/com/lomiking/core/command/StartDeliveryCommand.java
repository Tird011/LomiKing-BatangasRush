package com.lomiking.core.command;

import com.lomiking.core.engine.GameEngine;

/** Hop on the motorbike with a ready delivery order. */
public final class StartDeliveryCommand implements GameCommand {
    private final String orderId;   // null = first ready delivery
    public StartDeliveryCommand(String orderId) { this.orderId = orderId; }
    @Override public CommandResult execute(GameEngine e) { return e.startDelivery(orderId); }
    @Override public String name() { return "deliver"; }
}
