package com.lomiking.core.scene;

import com.lomiking.core.character.Emote;
import com.lomiking.core.cooking.CookingSession;
import com.lomiking.core.cooking.PlayerInput;
import com.lomiking.core.engine.GameEngine;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** Where the cooking mini-games run. Food only cooks while Mang Kaloy is in the kitchen. */
public class KitchenScene extends Scene {
    private CookingSession session;

    @Override public SceneId getId() { return SceneId.KITCHEN; }
    @Override public Set<SceneId> getExits() { return EnumSet.of(SceneId.COUNTER); }
    @Override public Emote getRestingEmote() { return Emote.HAPPY_MASKED; }
    @Override public String getEnvironmentModel() { return "kitchen_stove_kawali"; }

    @Override public void update(double dt, GameEngine engine) {
        if (session != null) session.update(dt, engine.cookingFeedback(), engine.cookingListener());
    }
    @Override public boolean handleInput(PlayerInput input, GameEngine engine) {
        if (session == null) return false;
        session.handleInput(input, engine.cookingFeedback(), engine.cookingListener());
        return true;
    }

    public CookingSession getSession() { return session; }
    public void setSession(CookingSession s) { session = s; }
    public void clearSession() { session = null; }

    @Override public Map<String, Object> viewState(GameEngine engine) {
        Map<String, Object> m = super.viewState(engine);
        if (session != null && !session.isFinished()) {
            m.put("cookingDish", session.getDish().getId());
            m.put("cookingOrder", session.getOrderId());
            m.put("stepIndex", session.getStepIndex());
            m.put("stepCount", session.getStepCount());
        }
        return m;
    }
}
