package com.lomiking.core.bridge;

import com.lomiking.core.command.*;
import com.lomiking.core.cooking.InputType;
import com.lomiking.core.cooking.PlayerInput;
import com.lomiking.core.scene.SceneId;
import java.util.Map;

/**
 * Turns JSON from React Native into GameCommand objects. Examples:
 * <pre>
 * {"cmd":"start"}
 * {"cmd":"goTo","scene":"KITCHEN"}
 * {"cmd":"takeOrder"}                       {"cmd":"takeOrder","customerId":"c3"}
 * {"cmd":"cook","orderId":"o1"}
 * {"cmd":"input","type":"SWIPE","value":0.6,"dx":1,"dy":0}
 * {"cmd":"input","type":"DRAG_DROP","itemId":"noodles"}
 * {"cmd":"input","type":"MIC_LEVEL","value":0.8}
 * {"cmd":"serve","orderId":"o1"}
 * {"cmd":"startTask","taskId":"t4"}
 * {"cmd":"buy","ingredientId":"egg","qty":10}
 * {"cmd":"deliver","orderId":"o2"}
 * {"cmd":"pause"} {"cmd":"resume"} {"cmd":"nextDay"}
 * </pre>
 */
public final class CommandParser {
    private CommandParser() { }

    public static GameCommand parse(String json) { return parse(Json.parseObject(json)); }

    public static GameCommand parse(Map<String, Object> m) {
        String cmd = str(m, "cmd");
        if (cmd == null) throw new IllegalArgumentException("Missing \"cmd\"");
        switch (cmd) {
            case "start": return new StartGameCommand();
            case "nextDay": return new NextDayCommand();
            case "pause": return new PauseCommand();
            case "resume": return new ResumeCommand();
            case "goTo": return new GoToCommand(SceneId.valueOf(required(m, "scene")));
            case "takeOrder": return new TakeOrderCommand(str(m, "customerId"));
            case "serve": return new ServeCommand(str(m, "orderId"));
            case "cook": return new StartCookingCommand(str(m, "orderId"));
            case "startTask": return new StartTaskCommand(required(m, "taskId"));
            case "buy": return new BuyCommand(required(m, "ingredientId"), (int) num(m, "qty", 1));
            case "deliver": return new StartDeliveryCommand(str(m, "orderId"));
            case "input":
                return new InputCommand(new PlayerInput(InputType.valueOf(required(m, "type")), num(m, "value", 0),
                        str(m, "itemId"), num(m, "dx", 0), num(m, "dy", 0)));
            default: throw new IllegalArgumentException("Unknown cmd: " + cmd);
        }
    }

    private static String str(Map<String, Object> m, String k) { Object v = m.get(k); return v == null ? null : String.valueOf(v); }
    private static String required(Map<String, Object> m, String k) {
        String v = str(m, k);
        if (v == null) throw new IllegalArgumentException("Missing \"" + k + "\"");
        return v;
    }
    private static double num(Map<String, Object> m, String k, double def) {
        Object v = m.get(k);
        return v instanceof Number ? ((Number) v).doubleValue() : def;
    }
}
