package com.lomiking.core.event;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleSupplier;

/** Publishes events to listeners (observer pattern). */
public class EventBus {
    private final List<GameEventListener> listeners = new ArrayList<>();
    private final DoubleSupplier clock;

    public EventBus(DoubleSupplier clock) { this.clock = clock; }

    public void subscribe(GameEventListener l) { listeners.add(l); }
    public void unsubscribe(GameEventListener l) { listeners.remove(l); }

    /** emit(TYPE, "key", value, "key2", value2 ...) */
    public void emit(EventType type, Object... keyValues) {
        Map<String, Object> data = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) data.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
        GameEvent e = new GameEvent(type, clock.getAsDouble(), data);
        for (GameEventListener l : new ArrayList<>(listeners)) l.onEvent(e);
    }
}
