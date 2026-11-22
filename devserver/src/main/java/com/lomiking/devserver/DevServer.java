package com.lomiking.devserver;

import com.lomiking.core.bridge.GameBridge;
import com.lomiking.core.bridge.Json;
import com.lomiking.core.engine.GameConfig;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Runs the real Java game logic on your PC over HTTP, so the React Native app can be played in a
 * browser (Expo web) or on a phone over Wi-Fi while developing. Android release builds don't use this:
 * they call the same GameBridge in-process through the native module.
 *
 *   POST /step    {"dt":0.05,"commands":[...]}  -> same JSON as GameBridge.step
 *   POST /reset   {"seed":7}                    -> new game (optional: dayLength, deliveryChance)
 *   GET  /catalog                               -> menu + ingredients
 *   GET  /save                                  -> progress JSON (the app stores it)
 *   POST /load    <save json>                   -> restore progress (title screen)
 */
public final class DevServer {
    private static volatile GameBridge bridge = new GameBridge();

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8787;
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/step", ex -> handle(ex, body -> {
            Map<String, Object> m = body.isEmpty() ? Map.of() : Json.parseObject(body);
            double dt = m.get("dt") instanceof Number ? ((Number) m.get("dt")).doubleValue() : 0;
            Object cmds = m.get("commands");
            return bridge.step(dt, cmds == null ? "[]" : Json.stringify(cmds));
        }));
        server.createContext("/reset", ex -> handle(ex, body -> {
            GameConfig cfg = GameConfig.defaults();
            if (!body.isEmpty()) {
                Map<String, Object> m = Json.parseObject(body);
                if (m.get("seed") instanceof Number) cfg.seed(((Number) m.get("seed")).longValue());
                if (m.get("dayLength") instanceof Number) cfg.dayLength(((Number) m.get("dayLength")).doubleValue());
                if (m.get("startLevel") instanceof Number) cfg.startLevel(((Number) m.get("startLevel")).intValue());
                if (m.get("money") instanceof Number) cfg.startingMoney = ((Number) m.get("money")).doubleValue();
                if (m.get("deliveryChance") instanceof Number) cfg.deliveryChance(((Number) m.get("deliveryChance")).doubleValue());
            }
            bridge = new GameBridge(cfg);
            return "{\"ok\":true}";
        }));
        server.createContext("/catalog", ex -> handle(ex, body -> bridge.catalog()));
        server.createContext("/save", ex -> handle(ex, body -> bridge.exportSave()));
        server.createContext("/load", ex -> handle(ex, body -> bridge.importSave(body)));
        server.createContext("/", ex -> handle(ex, body -> "{\"name\":\"LomiKing dev server\",\"ok\":true}"));
        server.start();
        System.out.println("LomiKing Java logic running on http://localhost:" + port + "  (Ctrl+C to stop)");
    }

    interface Handler { String apply(String body) throws Exception; }

    private static void handle(HttpExchange ex, Handler h) throws IOException {
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        if ("OPTIONS".equals(ex.getRequestMethod())) { ex.sendResponseHeaders(204, -1); ex.close(); return; }
        String out; int code = 200;
        try (InputStream in = ex.getRequestBody()) {
            out = h.apply(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            code = 400;
            out = Json.stringify(Map.of("ok", false, "message", String.valueOf(e.getMessage())));
        }
        byte[] bytes = out.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }
}
