package com.lomiking.core.task;

import com.lomiking.core.scene.SceneId;

/** The three work areas notes are pinned to (plus the counter itself). */
public enum Sector {
    COUNTER(SceneId.COUNTER), KITCHEN(SceneId.KITCHEN), MAIN_DINER(SceneId.MAIN_DINER), DELIVERY_HUB(SceneId.DELIVERY_HUB);

    private final SceneId scene;
    Sector(SceneId scene) { this.scene = scene; }
    public SceneId getScene() { return scene; }
}
