package com.lomiking.core.bridge;

import com.lomiking.core.command.CommandResult;
import com.lomiking.core.command.GameCommand;
import com.lomiking.core.engine.GameConfig;
import com.lomiking.core.engine.GameEngine;
import com.lomiking.core.event.GameEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The one class React Native talks to. Strings in, strings out, so it maps directly onto a
 * React Native native module (Android) and the JS side never touches Java objects:
 * <pre>
 *   dispatch('{"cmd":"goTo","scene":"KITCHEN"}')  -> '{"ok":true,"message":"Now in KITCHEN"}'
 *   tick(0.016)                                    advance the simulation one frame
 *   snapshot()                                     -> full world state for three.js to draw
 *   drainEvents()                                  -> events since the last call (sounds, animations)
 *   catalog()                                      -> menu + ingredients (load once)
 * </pre>
 * All methods are synchronized so the RN thread and a game-loop thread can share one bridge.
 */
public class GameBridge {
    private final GameEngine engine;
    private final List<GameEvent> pending = new ArrayList<>();

    public GameBridge(GameConfig config) {
        engine = new GameEngine(config);
        engine.subscribe(e -> { synchronized (pending) { pending.add(e); } });
    }
    public GameBridge() { this(GameConfig.defaults()); }

    public synchronized String dispatch(String commandJson) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            GameCommand cmd = CommandParser.parse(commandJson);
            CommandResult r = cmd.execute(engine);
            out.put("ok", r.isOk());
            out.put("message", r.getMessage());
        } catch (RuntimeException ex) {
            out.put("ok", false);
            out.put("message", "Bad command: " + ex.getMessage());
        }
        return Json.stringify(out);
    }

    public synchronized void tick(double dtSeconds) { engine.update(dtSeconds); }

    /**
     * One round trip per frame (best for React Native): run the queued commands, advance time,
     * and return everything the UI needs.
     * @param commandsJson a JSON array of command objects, e.g. [{"cmd":"input","type":"TAP"}]
     * @return {"results":[{ok,message}...],"events":[...],"snapshot":{...}}
     */
    @SuppressWarnings("unchecked")
    public synchronized String step(double dtSeconds, String commandsJson) {
        List<Object> results = new ArrayList<>();
        if (commandsJson != null && !commandsJson.trim().isEmpty()) {
            Object parsed;
            try { parsed = Json.parse(commandsJson); } catch (RuntimeException ex) { parsed = null; results.add(fail("Bad JSON: " + ex.getMessage())); }
            if (parsed instanceof List) {
                for (Object o : (List<Object>) parsed) {
                    if (!(o instanceof Map)) { results.add(fail("Command must be an object")); continue; }
                    results.add(run((Map<String, Object>) o));
                }
            }
        }
        engine.update(dtSeconds);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("results", results);
        out.put("events", Json.parse(drainEvents()));
        out.put("snapshot", SnapshotBuilder.build(engine));
        return Json.stringify(out);
    }

    private Map<String, Object> run(Map<String, Object> cmdJson) {
        Map<String, Object> r = new LinkedHashMap<>();
        try {
            CommandResult res = CommandParser.parse(cmdJson).execute(engine);
            r.put("cmd", cmdJson.get("cmd"));
            r.put("ok", res.isOk());
            r.put("message", res.getMessage());
        } catch (RuntimeException ex) {
            return fail("Bad command: " + ex.getMessage());
        }
        return r;
    }

    private static Map<String, Object> fail(String msg) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("ok", false);
        r.put("message", msg);
        return r;
    }

    public synchronized String snapshot() { return Json.stringify(SnapshotBuilder.build(engine)); }

    public synchronized String catalog() { return Json.stringify(SnapshotBuilder.catalog(engine)); }

    /** The player's progress as JSON, to store on the phone. */
    public synchronized String exportSave() { return engine.exportSave(); }

    /** Restore progress (title screen only). @return {"ok":..,"message":..} */
    public synchronized String importSave(String json) {
        CommandResult r = engine.importSave(json);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ok", r.isOk()); out.put("message", r.getMessage());
        return Json.stringify(out);
    }

    public String drainEvents() {
        List<Object> list = new ArrayList<>();
        synchronized (pending) {
            for (GameEvent e : pending) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("type", e.getType().name());
                m.put("t", Math.round(e.getTime() * 100) / 100.0);
                m.putAll(e.getData());
                list.add(m);
            }
            pending.clear();
        }
        return Json.stringify(list);
    }

    /** For Java-side renderers/tests that want the engine directly. */
    public GameEngine getEngine() { return engine; }
}
