package com.lomiking.core.cooking;

import com.lomiking.core.character.Emote;

/** What a cooking step may report back to the game while the player works on it. */
public interface CookingFeedback {
    void mistake(String reason);
    void burnt(String what);
    void emote(Emote emote, double seconds);
}
