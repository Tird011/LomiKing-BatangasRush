package com.lomiking.core.cooking;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Drag the right ingredients into the pot or wok, in any order. Wrong items count as mistakes. */
public class AddIngredientsStep extends CookingStep {
    private final List<String> required;
    private final Set<String> added = new LinkedHashSet<>();

    public AddIngredientsStep(String title, List<String> ingredientIds) {
        super(title);
        this.required = new ArrayList<>(ingredientIds);
        if (required.isEmpty()) markComplete();
    }
    @Override public InputType getGesture() { return InputType.DRAG_DROP; }
    @Override public String getHint() { return "Drag these into the pot: " + String.join(", ", remaining()); }
    @Override public double getProgress() { return required.isEmpty() ? 1 : (double) added.size() / required.size(); }
    @Override public double getScore() { return clamp01(1 - 0.2 * getMistakes()); }

    public List<String> remaining() {
        List<String> r = new ArrayList<>(required);
        r.removeAll(added);
        return r;
    }

    @Override protected void onInput(PlayerInput in, CookingFeedback fb) {
        if (in.getType() != InputType.DRAG_DROP || in.getItemId() == null) return;
        String id = in.getItemId();
        if (!required.contains(id)) { addMistake(fb, "That doesn't go in here: " + id); return; }
        if (!added.add(id)) { addMistake(fb, "Already added " + id); return; }
        if (added.size() == required.size()) markComplete();
    }
    @Override protected void fillViewState(Map<String, Object> m) {
        m.put("required", String.join(",", required));
        m.put("remaining", String.join(",", remaining()));
    }
}
