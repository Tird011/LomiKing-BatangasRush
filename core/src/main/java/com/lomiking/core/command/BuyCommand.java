package com.lomiking.core.command;

import com.lomiking.core.engine.GameEngine;

/** Buy ingredients at the market (delivery hub). */
public final class BuyCommand implements GameCommand {
    private final String ingredientId;
    private final int qty;
    public BuyCommand(String ingredientId, int qty) { this.ingredientId = ingredientId; this.qty = qty; }
    @Override public CommandResult execute(GameEngine e) { return e.buy(ingredientId, qty); }
    @Override public String name() { return "buy"; }
}
