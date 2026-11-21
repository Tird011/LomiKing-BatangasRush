package com.lomiking.core.scene;

import com.lomiking.core.cooking.PlayerInput;
import com.lomiking.core.delivery.DeliveryMiniGame;
import com.lomiking.core.engine.GameEngine;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** The motorbike out back: start delivery runs and buy supplies at the market. */
public class DeliveryHubScene extends Scene {
    private DeliveryMiniGame run;

    @Override public SceneId getId() { return SceneId.DELIVERY_HUB; }
    @Override public Set<SceneId> getExits() { return EnumSet.of(SceneId.COUNTER); }
    @Override public String getEnvironmentModel() { return run == null ? "delivery_hub_market" : "batangas_road"; }

    @Override public void update(double dt, GameEngine engine) { if (run != null) run.update(dt); }
    @Override public boolean handleInput(PlayerInput input, GameEngine engine) {
        if (run == null) return false;
        run.handleInput(input);
        return true;
    }
    @Override public String blockLeavingReason() { return run != null ? "Finish the delivery first!" : null; }

    public DeliveryMiniGame getRun() { return run; }
    public void setRun(DeliveryMiniGame r) { run = r; }
    public void clearRun() { run = null; }

    @Override public Map<String, Object> viewState(GameEngine engine) {
        Map<String, Object> m = super.viewState(engine);
        m.put("delivering", run != null);
        return m;
    }
}
