package com.lomiking.core.scene;

import com.lomiking.core.engine.GameEngine;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** Title screen: logo, Start button and the lomi house model spinning behind it. */
public class MenuScene extends Scene {
    @Override public SceneId getId() { return SceneId.MENU; }
    @Override public Set<SceneId> getExits() { return EnumSet.of(SceneId.COUNTER); }
    @Override public String getEnvironmentModel() { return "lomi_house_exterior"; }
    @Override public Map<String, Object> viewState(GameEngine engine) {
        Map<String, Object> m = super.viewState(engine);
        m.put("logo", "lomiking_logo");
        m.put("buttons", "start");
        return m;
    }
}
