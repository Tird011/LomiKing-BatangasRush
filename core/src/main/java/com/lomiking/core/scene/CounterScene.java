package com.lomiking.core.scene;

import java.util.EnumSet;
import java.util.Set;

/** The hub: customers line up here; take orders and serve ready food. Every other room is one step away. */
public class CounterScene extends Scene {
    @Override public SceneId getId() { return SceneId.COUNTER; }
    @Override public Set<SceneId> getExits() { return EnumSet.of(SceneId.KITCHEN, SceneId.MAIN_DINER, SceneId.DELIVERY_HUB); }
    @Override public String getEnvironmentModel() { return "counter_line"; }
}
