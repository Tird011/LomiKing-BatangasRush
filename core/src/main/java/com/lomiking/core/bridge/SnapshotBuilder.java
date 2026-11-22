package com.lomiking.core.bridge;

import com.lomiking.core.character.Emote;
import com.lomiking.core.cooking.CookingSession;
import com.lomiking.core.customer.Customer;
import com.lomiking.core.customer.Order;
import com.lomiking.core.customer.OrderLine;
import com.lomiking.core.delivery.DeliveryMiniGame;
import com.lomiking.core.delivery.Obstacle;
import com.lomiking.core.engine.GameEngine;
import com.lomiking.core.food.Dish;
import com.lomiking.core.shop.Table;
import com.lomiking.core.task.Task;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Builds a plain Map/List picture of everything the renderer needs to draw this frame. */
public final class SnapshotBuilder {
    private SnapshotBuilder() { }

    public static Map<String, Object> build(GameEngine e) {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("state", e.getState().name());
        s.put("day", e.getDay());
        s.put("time", r(e.getTime()));
        s.put("dayLength", e.getConfig().dayLengthSeconds);
        s.put("closed", e.isClosed());
        s.put("money", r(e.wallet().getBalance()));
        s.put("reputation", r(e.getReputation()));
        s.put("scene", e.getCurrentScene().viewState(e));

        Emote em = e.getKaloy().getEmote();
        Map<String, Object> k = new LinkedHashMap<>();
        k.put("emote", em.name()); k.put("atlasCol", em.getAtlasCol()); k.put("atlasRow", em.getAtlasRow());
        s.put("kaloy", k);

        List<Object> customers = new ArrayList<>();
        for (Customer c : e.getCustomers()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getId()); m.put("name", c.getName()); m.put("type", c.getTypeLabel()); m.put("model", c.getModelId());
            m.put("action", c.getAction().name()); m.put("emotion", c.getEmotion().name()); m.put("patience", r(c.getPatienceRatio()));
            m.put("remote", c.isRemote()); m.put("say", c.greeting());
            if (c.getRating() != null) { m.put("stars", c.getRating().getStars()); m.put("comment", c.getRating().getComment()); } m.put("tableId", c.getTableId());
            m.put("orderId", c.getOrder() == null ? null : c.getOrder().getId());
            customers.add(m);
        }
        s.put("customers", customers);

        List<Object> orders = new ArrayList<>();
        for (Order o : e.getOrders()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", o.getId()); m.put("customerId", o.getCustomerId()); m.put("status", o.getStatus().name());
            m.put("delivery", o.isDelivery()); m.put("summary", o.summary()); m.put("price", o.totalPrice());
            List<Object> lines = new ArrayList<>();
            for (OrderLine l : o.getLines()) {
                Map<String, Object> lm = new LinkedHashMap<>();
                lm.put("dish", l.getDish().getId()); lm.put("extras", l.getExtras());
                lm.put("cooking", l.isCooking()); lm.put("cooked", l.isCooked());
                if (l.isCooked()) { lm.put("quality", r(l.getCooked().getQuality())); lm.put("stars", l.getCooked().getCookStars()); }
                lines.add(lm);
            }
            m.put("lines", lines);
            orders.add(m);
        }
        s.put("orders", orders);

        CookingSession cs = e.getCookingSession();
        if (cs != null && !cs.isFinished()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("orderId", cs.getOrderId()); m.put("dish", cs.getDish().getId());
            m.put("stepIndex", cs.getStepIndex()); m.put("stepCount", cs.getStepCount());
            m.put("step", cs.currentStepView());
            s.put("cooking", m);
        } else s.put("cooking", null);

        List<Object> tasks = new ArrayList<>();
        for (Task t : e.getTaskBoard().open()) tasks.add(t.viewState());
        s.put("tasks", tasks);

        List<Object> tables = new ArrayList<>();
        for (Table t : e.shop().getTables()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", t.getId()); m.put("occupant", t.getOccupantId()); m.put("dirt", r(t.getDirt()));
            tables.add(m);
        }
        Map<String, Object> shop = new LinkedHashMap<>();
        shop.put("tables", tables); shop.put("dirtyDishes", e.shop().getDirtyDishes()); shop.put("floorDirt", r(e.shop().getFloorDirt()));
        s.put("shop", shop);

        s.put("inventory", new LinkedHashMap<>(e.inventory().view()));

        com.lomiking.core.economy.DayReport rep = e.getReport();
        Map<String, Object> rm = new LinkedHashMap<>();
        rm.put("day", rep.getDay()); rm.put("served", rep.getCustomersServed()); rm.put("angry", rep.getCustomersAngry());
        rm.put("deliveries", rep.getDeliveriesDone()); rm.put("deliveriesFailed", rep.getDeliveriesFailed());
        rm.put("dishesCooked", rep.getDishesCooked()); rm.put("tasksDone", rep.getTasksDone());
        rm.put("revenue", r(rep.getRevenue())); rm.put("tips", r(rep.getTips())); rm.put("spent", r(rep.getSpent()));
        rm.put("profit", r(rep.getProfit())); rm.put("avgRating", r(rep.getAverageRating()));
        rm.put("fiveStars", rep.getFiveStars()); rm.put("perfectDishes", rep.getPerfectDishes());
        rm.put("wages", r(rep.getWages())); rm.put("bonuses", r(rep.getBonuses())); rm.put("xpGained", r(rep.getXpGained()));
        rm.put("goalsDone", e.getProgress().goalsDone());
        s.put("report", rm);

        // progression, goals, shop, staff
        s.put("progress", e.getProgress().view());
        s.put("goals", e.getProgress().goalsView(rep));
        List<Object> ups = new ArrayList<>();
        for (com.lomiking.core.upgrade.Upgrade u : e.getUpgrades().all())
            ups.add(u.view(e.getUpgrades().level(u.getId()), e.getProgress().getLevel(), e.wallet().getBalance()));
        s.put("upgrades", ups);
        List<Object> staff = new ArrayList<>();
        for (com.lomiking.core.staff.Worker w : e.getStaff().all()) {
            Map<String, Object> wm = w.view(e.getProgress().getLevel(), e.wallet().getBalance());
            if (w instanceof com.lomiking.core.staff.CookWorker) wm.put("progress", r(((com.lomiking.core.staff.CookWorker) w).getProgressRatio()));
            if (w instanceof com.lomiking.core.staff.RiderWorker) wm.put("out", ((com.lomiking.core.staff.RiderWorker) w).isOut());
            staff.add(wm);
        }
        s.put("staff", staff);
        s.put("achievements", e.getProgress().achievementsView());
        Map<String, Object> pk = new LinkedHashMap<>();
        pk.put("knife", e.getPerks().getKnife()); pk.put("stove", e.getPerks().getStove()); pk.put("recipe", e.getPerks().getRecipe());
        pk.put("decor", e.getUpgrades().level("decor")); pk.put("sign", e.getUpgrades().level("sign")); pk.put("scooter", e.getUpgrades().level("scooter"));
        s.put("perks", pk);

        DeliveryMiniGame run = e.getDeliveryRun();
        if (run != null) {
            Map<String, Object> m = run.viewState();
            List<Object> obs = new ArrayList<>();
            for (Obstacle o : run.getObstacles()) {
                if (o.isPassed() || o.getPosition() - run.getDistance() > 120) continue;   // only what's on screen
                Map<String, Object> om = new LinkedHashMap<>();
                om.put("type", o.getType().name()); om.put("lane", o.getLane()); om.put("ahead", r(o.getPosition() - run.getDistance()));
                obs.add(om);
            }
            m.put("obstacles", obs);
            s.put("delivery", m);
        } else s.put("delivery", null);
        return s;
    }

    /** Static data the UI needs once: the menu and ingredient list. */
    public static Map<String, Object> catalog(GameEngine e) {
        Map<String, Object> s = new LinkedHashMap<>();
        List<Object> menu = new ArrayList<>();
        for (Dish d : e.getFullMenu().all()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", d.getId()); m.put("name", d.getName()); m.put("price", d.getPrice());
            m.put("category", d.getCategory().name()); m.put("ingredients", d.getIngredientIds()); m.put("extras", d.getPossibleExtras());
            menu.add(m);
        }
        s.put("menu", menu);
        List<Object> ingredients = new ArrayList<>();
        for (com.lomiking.core.food.Ingredient i : e.getCatalog().all()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", i.getId()); m.put("name", i.getDisplayName()); m.put("category", i.getCategory().name());
            m.put("prep", i.getPrepMethod().name()); m.put("cost", i.getUnitCost());
            ingredients.add(m);
        }
        s.put("ingredients", ingredients);
        return s;
    }

    private static double r(double v) { return Math.round(v * 100) / 100.0; }
}
