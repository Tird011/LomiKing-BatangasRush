package com.lomiking.core.character;

/**
 * The player character. Holds his current expression: a reaction shows for a few seconds,
 * then he goes back to the resting face of the room he is in.
 */
public class MangKaloy {
    private Emote restingEmote = Emote.HAPPY;
    private Emote currentEmote = Emote.HAPPY;
    private double reactionTimeLeft;

    /** @return true if the visible face changed */
    public boolean react(Emote emote, double seconds) {
        reactionTimeLeft = Math.max(seconds, 0);
        return setCurrent(emote);
    }

    /** @return true if the visible face changed */
    public boolean setRestingEmote(Emote emote) {
        restingEmote = emote;
        return reactionTimeLeft <= 0 && setCurrent(emote);
    }

    /** @return true if the visible face changed */
    public boolean update(double dt) {
        if (reactionTimeLeft > 0) {
            reactionTimeLeft -= dt;
            if (reactionTimeLeft <= 0) return setCurrent(restingEmote);
        }
        return false;
    }

    private boolean setCurrent(Emote e) {
        boolean changed = e != currentEmote;
        currentEmote = e;
        return changed;
    }

    public Emote getEmote() { return currentEmote; }
    public Emote getRestingEmote() { return restingEmote; }
}
