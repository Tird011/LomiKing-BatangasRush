package com.lomiking.core.event;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** An immutable "something happened" message. The payload holds plain values only (String, Number, Boolean). */
public final class GameEvent {
    private final EventType type;
    private final double time;
    private final Map<String, Object> data;

    public GameEvent(EventType type, double time, Map<String, Object> data) {
        this.type = type;
        this.time = time;
        this.data = Collections.unmodifiableMap(new LinkedHashMap<>(data));
    }

    public EventType getType() { return type; }
    public double getTime() { return time; }
    public Map<String, Object> getData() { return data; }
    public Object get(String key) { return data.get(key); }

    @Override public String toString() { return type + " " + data; }
}
