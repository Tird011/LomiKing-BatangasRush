package com.lomiking.core.scene;

import com.lomiking.core.character.Emote;
import com.lomiking.core.cooking.PlayerInput;
import com.lomiking.core.engine.GameEngine;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A room of the shop (state pattern). The engine forwards time and player input to the current scene,
 * and each scene decides what that input means in that room.
 */
public abstract class Scene {
    public abstract SceneId getId();
    /** Where the player can walk from here (the counter is the hub, see game_flow.png). */
    public abstract Set<SceneId> getExits();
    /** Mang Kaloy's face while in this room. */
    public Emote getRestingEmote() { return Emote.HAPPY; }
    /** Id of the 3D environment the renderer should show. */
    public abstract String getEnvironmentModel();

    public void onEnter(GameEngine engine) { }
    public void onExit(GameEngine engine) { }
    public void update(double dt, GameEngine engine) { }
    /** @return false if this room has nothing to do with the input right now */
    public boolean handleInput(PlayerInput input, GameEngine engine) { return false; }
    /** Some activities lock you in the room (you can't leave the motorbike mid-delivery). */
    public String blockLeavingReason() { return null; }

    public final boolean canGoTo(SceneId target) { return getExits().contains(target); }

    public Map<String, Object> viewState(GameEngine engine) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", getId().name());
        m.put("environment", getEnvironmentModel());
        List<String> exits = new ArrayList<>();
        for (SceneId id : getExits()) exits.add(id.name());
        m.put("exits", exits);
        return m;
    }
}
