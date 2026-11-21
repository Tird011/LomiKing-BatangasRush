package com.lomiking.core.command;

/** Whether an action was accepted, and a message to show (or why not). */
public final class CommandResult {
    private final boolean ok;
    private final String message;
    private CommandResult(boolean ok, String message) { this.ok = ok; this.message = message; }
    public static CommandResult ok(String msg) { return new CommandResult(true, msg); }
    public static CommandResult fail(String msg) { return new CommandResult(false, msg); }
    public boolean isOk() { return ok; }
    public String getMessage() { return message; }
    @Override public String toString() { return (ok ? "OK " : "NO ") + message; }
}
