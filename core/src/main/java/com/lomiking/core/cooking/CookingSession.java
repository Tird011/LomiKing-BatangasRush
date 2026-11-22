package com.lomiking.core.cooking;

import com.lomiking.core.food.Dish;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Runs a dish's steps one after another and produces a CookedDish at the end. */
public class CookingSession {
    /** Notified when steps change, so the engine can emit events. */
    public interface Listener {
        void stepStarted(CookingSession s, CookingStep step);
        void stepCompleted(CookingSession s, CookingStep step);
        void finished(CookingSession s, CookedDish result);
    }

    private final String orderId;
    private final int lineIndex;
    private final Dish dish;
    private final List<CookingStep> steps;
    private int current;
    private CookedDish result;

    private final KitchenPerks perks;

    public CookingSession(String orderId, int lineIndex, Dish dish, List<String> extras) { this(orderId, lineIndex, dish, extras, KitchenPerks.NONE); }

    public CookingSession(String orderId, int lineIndex, Dish dish, List<String> extras, KitchenPerks perks) {
        this.orderId = orderId; this.lineIndex = lineIndex; this.dish = dish; this.perks = perks;
        this.steps = dish.createSteps(extras, perks);
    }

    public void begin(Listener l) {
        l.stepStarted(this, steps.get(0));
        advanceIfDone(l);
    }

    public void handleInput(PlayerInput input, CookingFeedback fb, Listener l) {
        if (isFinished()) return;
        getCurrentStep().handleInput(input, fb);
        advanceIfDone(l);
    }

    public void update(double dt, CookingFeedback fb, Listener l) {
        if (isFinished()) return;
        getCurrentStep().update(dt, fb);
        advanceIfDone(l);
    }

    private void advanceIfDone(Listener l) {
        while (!isFinished() && getCurrentStep().isComplete()) {
            l.stepCompleted(this, getCurrentStep());
            current++;
            if (current < steps.size()) l.stepStarted(this, getCurrentStep());
            else { result = buildResult(); l.finished(this, result); }
        }
    }

    private CookedDish buildResult() {
        double sum = 0; boolean burnt = false;
        for (CookingStep s : steps) { sum += s.getScore(); burnt |= s.isBurnt(); }
        double quality = Math.min(1, sum / steps.size() + perks.qualityBonus());
        if (burnt) quality = Math.min(quality, 0.35);
        return new CookedDish(dish, quality, burnt);
    }

    public boolean isFinished() { return current >= steps.size(); }
    public CookingStep getCurrentStep() { return isFinished() ? null : steps.get(current); }
    public int getStepIndex() { return current; }
    public int getStepCount() { return steps.size(); }
    public List<CookingStep> getSteps() { return Collections.unmodifiableList(steps); }
    public Dish getDish() { return dish; }
    public String getOrderId() { return orderId; }
    public int getLineIndex() { return lineIndex; }
    public CookedDish getResult() { return result; }
    public Map<String, Object> currentStepView() { return isFinished() ? null : getCurrentStep().viewState(); }
}
