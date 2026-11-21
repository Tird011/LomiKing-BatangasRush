package com.lomiking.core;

import com.lomiking.core.cooking.*;
import com.lomiking.core.delivery.DeliveryMiniGame;
import com.lomiking.core.delivery.Obstacle;
import com.lomiking.core.engine.GameEngine;
import com.lomiking.core.task.Task;
import java.util.Map;

/** A bot that plays the mini-games well. Used by tests and the playthrough demo. */
public final class AutoPlayer {
    private AutoPlayer() { }

    /** Plays the current cooking session until the dish is done. */
    public static void cookCurrentDish(GameEngine e) {
        int guard = 0;
        while (e.getCookingSession() != null && guard++ < 5000) {
            CookingStep step = e.getCookingSession().getCurrentStep();
            Map<String, Object> v = step.viewState();
            if (step instanceof ChopStep) e.input(PlayerInput.swipe(0.6, 1, 0));
            else if (step instanceof AddIngredientsStep) e.input(PlayerInput.drop(((AddIngredientsStep) step).remaining().get(0)));
            else if (step instanceof StirStep) e.input(PlayerInput.circle(1));
            else if (step instanceof SeasonStep) {
                if ((int) v.get("shakes") < (int) v.get("target")) e.input(PlayerInput.tap()); else e.input(PlayerInput.confirm());
            } else if (step instanceof PlateStep) {
                PlateStep p = (PlateStep) step;
                if (!p.missing().isEmpty()) e.input(PlayerInput.drop(p.missing().get(0))); else e.input(PlayerInput.confirm());
            } else if (step instanceof FryStep) {
                double el = (double) v.get("elapsed"), target = (double) v.get("target");
                boolean flipPending = (boolean) v.get("needsFlip") && !(boolean) v.get("flipped");
                if (flipPending ? el >= target / 2 : el >= target) e.input(PlayerInput.tap());
            } else if (step instanceof BoilStep) {
                if ((double) v.get("elapsed") >= (double) v.get("target")) e.input(PlayerInput.tap());
            } else if (step instanceof FanFireStep) {
                e.input(PlayerInput.mic(((FanFireStep) step).getHeat() < 0.7 ? 0.9 : 0.0));
            }
            e.update(0.1);
        }
    }

    /** Rides the delivery, dodging into a lane without an obstacle coming up. */
    public static void rideDelivery(GameEngine e) {
        int guard = 0;
        while (e.getDeliveryRun() != null && guard++ < 10000) {
            DeliveryMiniGame run = e.getDeliveryRun();
            for (Obstacle o : run.getObstacles()) {
                double ahead = o.getPosition() - run.getDistance();
                if (!o.isPassed() && ahead > 0 && ahead < 12 && o.getLane() == run.getLane()) {
                    int target = run.getLane() == 0 ? 1 : run.getLane() - 1;
                    e.input(PlayerInput.swipe(0.5, target < run.getLane() ? -1 : 1, 0));
                    break;
                }
            }
            e.update(0.05);
        }
    }

    /** Finishes an interactive chore. */
    public static void doChore(GameEngine e, Task t) {
        int guard = 0;
        while (!t.isFinished() && guard++ < 500) {
            switch (t.getClass().getSimpleName()) {
                case "CleanTableTask": e.input(PlayerInput.swipe(0.8, 1, 0)); break;
                case "WashDishesTask": e.input(PlayerInput.tap()); break;
                default: e.input(PlayerInput.circle(1)); break;
            }
            e.update(0.05);
        }
    }
}
