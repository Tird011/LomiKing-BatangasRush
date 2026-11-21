package com.lomiking.core.cooking;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Plate up and add the side condiments the customer asked for (calamansi, sili...), then press Done. */
public class PlateStep extends CookingStep {
    private final List<String> requestedExtras;
    private final List<String> added = new ArrayList<>();

    public PlateStep(List<String> requestedExtras) {
        super("Plate it up");
        this.requestedExtras = new ArrayList<>(requestedExtras);
    }
    @Override public InputType getGesture() { return requestedExtras.isEmpty() ? InputType.CONFIRM : InputType.DRAG_DROP; }
    @Override public String getHint() {
        return requestedExtras.isEmpty() ? "Press Done to plate" : "Add on the side: " + String.join(", ", missing()) + ", then Done";
    }
    @Override public double getProgress() { return requestedExtras.isEmpty() ? 0 : (double) (requestedExtras.size() - missing().size()) / requestedExtras.size(); }
    @Override public double getScore() { return clamp01(1 - 0.2 * getMistakes() - 0.25 * missing().size()); }

    public List<String> missing() {
        List<String> m = new ArrayList<>(requestedExtras);
        m.removeAll(added);
        return m;
    }
    @Override protected void onInput(PlayerInput in, CookingFeedback fb) {
        if (in.getType() == InputType.DRAG_DROP && in.getItemId() != null) {
            if (requestedExtras.contains(in.getItemId()) && !added.contains(in.getItemId())) added.add(in.getItemId());
            else addMistake(fb, "They didn't ask for " + in.getItemId());
        } else if (in.getType() == InputType.CONFIRM) {
            markComplete();
        }
    }
    @Override protected void fillViewState(Map<String, Object> m) {
        m.put("requested", String.join(",", requestedExtras)); m.put("missing", String.join(",", missing()));
    }
}
