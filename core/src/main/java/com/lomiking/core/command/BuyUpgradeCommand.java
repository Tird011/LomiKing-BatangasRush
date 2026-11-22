package com.lomiking.core.command;

import com.lomiking.core.engine.GameEngine;

/** Buy the next level of an upgrade. */
public final class BuyUpgradeCommand implements GameCommand {
    private final String id;
    public BuyUpgradeCommand(String id) { this.id = id; }
    @Override public CommandResult execute(GameEngine e) { return e.buyUpgrade(id); }
    @Override public String name() { return "buyUpgrade"; }
}
