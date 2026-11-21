package com.lomiking.core;

import com.lomiking.core.customer.Customer;
import com.lomiking.core.customer.CustomerAction;
import com.lomiking.core.customer.Order;
import com.lomiking.core.customer.OrderStatus;
import com.lomiking.core.engine.GameConfig;
import com.lomiking.core.engine.GameEngine;
import com.lomiking.core.engine.GameState;
import com.lomiking.core.event.EventType;
import com.lomiking.core.scene.SceneId;
import com.lomiking.core.task.Task;
import java.util.EnumSet;
import java.util.Set;

/** Plays a whole day with the bot and prints the story. Handy to see the logic working without any graphics. */
public class PlaythroughDemo {
    static final Set<EventType> QUIET = EnumSet.of(EventType.COOKING_STEP_STARTED, EventType.COOKING_STEP_COMPLETED,
            EventType.EMOTE_CHANGED, EventType.TASK_PROGRESS, EventType.CUSTOMER_EMOTION_CHANGED, EventType.MONEY_CHANGED,
            EventType.SCENE_CHANGED, EventType.COMMAND_REJECTED, EventType.LOW_STOCK);

    public static void main(String[] args) {
        GameEngine e = new GameEngine(GameConfig.defaults().dayLength(150));
        e.subscribe(ev -> { if (!QUIET.contains(ev.getType())) System.out.printf("[%6.1fs] %-22s %s%n", ev.getTime(), ev.getType(), ev.getData()); });
        e.startGame();
        while (e.getState() == GameState.PLAYING) {
            boolean acted = false;
            // 1) cook anything waiting
            for (Order o : e.getOrders()) if (o.isOpen() && o.nextLineToCook() >= 0) {
                go(e, SceneId.KITCHEN);
                if (e.startCooking(o.getId()).isOk()) { AutoPlayer.cookCurrentDish(e); acted = true; }
                break;
            }
            // 2) serve / deliver what's ready
            for (Order o : e.getOrders()) if (o.getStatus() == OrderStatus.READY) {
                if (o.isDelivery()) { go(e, SceneId.DELIVERY_HUB); if (e.startDelivery(o.getId()).isOk()) { AutoPlayer.rideDelivery(e); acted = true; } }
                else { go(e, SceneId.COUNTER); acted |= e.serve(o.getId()).isOk(); }
            }
            // 3) take orders
            for (Customer c : e.getCustomers()) if (c.getAction() == CustomerAction.IN_LINE) {
                go(e, SceneId.COUNTER);
                if (e.takeOrder(c.getId()).isOk()) acted = true;
                break;
            }
            // 4) chores and shopping
            for (Task t : e.getTaskBoard().open()) {
                if (t.isInteractive()) { go(e, t.getSector().getScene()); if (e.startTask(t.getId()).isOk()) { AutoPlayer.doChore(e, t); acted = true; } break; }
                if (t instanceof com.lomiking.core.task.RestockTask) {
                    go(e, SceneId.DELIVERY_HUB);
                    if (e.buy(((com.lomiking.core.task.RestockTask) t).getIngredientId(), 10).isOk()) { e.update(0.1); acted = true; }
                    break;
                }
            }
            if (!acted) e.update(0.2);
        }
        System.out.printf("%nDay %d over: served %d, angry %d, deliveries %d, revenue P%.0f, tips P%.0f, spent P%.0f, avg %.2f stars, money P%.0f%n",
            e.getDay(), e.getReport().getCustomersServed(), e.getReport().getCustomersAngry(), e.getReport().getDeliveriesDone(),
            e.getReport().getRevenue(), e.getReport().getTips(), e.getReport().getSpent(), e.getReport().getAverageRating(), e.wallet().getBalance());
    }

    private static void go(GameEngine e, SceneId target) {
        if (e.getCurrentScene().getId() == target) return;
        if (e.getCurrentScene().getId() != SceneId.COUNTER) e.goTo(SceneId.COUNTER);
        e.goTo(target);
    }
}
