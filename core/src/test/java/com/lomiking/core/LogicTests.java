package com.lomiking.core;

import com.lomiking.core.bridge.GameBridge;
import com.lomiking.core.bridge.Json;
import com.lomiking.core.character.Emote;
import com.lomiking.core.cooking.*;
import com.lomiking.core.customer.*;
import com.lomiking.core.economy.Inventory;
import com.lomiking.core.engine.GameConfig;
import com.lomiking.core.engine.GameEngine;
import com.lomiking.core.engine.GameState;
import com.lomiking.core.event.EventType;
import com.lomiking.core.food.*;
import com.lomiking.core.scene.SceneId;
import com.lomiking.core.task.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Dependency-free test runner (no JUnit needed): every public static void test*() method runs.
 * Run: gradle :core:test   or   java -cp ... com.lomiking.core.LogicTests
 */
public class LogicTests {
    static final CookingFeedback NO_FEEDBACK = new CookingFeedback() {
        public void mistake(String r) { }
        public void burnt(String w) { }
        public void emote(Emote e, double s) { }
    };

    public static void main(String[] args) throws Exception {
        int pass = 0, fail = 0;
        for (Method m : LogicTests.class.getDeclaredMethods()) {
            if (!m.getName().startsWith("test") || !Modifier.isStatic(m.getModifiers())) continue;
            try { m.invoke(null); pass++; System.out.println("  PASS " + m.getName()); }
            catch (Exception ex) {
                fail++;
                Throwable c = ex.getCause() != null ? ex.getCause() : ex;
                System.out.println("  FAIL " + m.getName() + ": " + c);
            }
        }
        System.out.println(pass + " passed, " + fail + " failed");
        if (fail > 0) System.exit(1);
    }

    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); }

    // ---------------- helpers ----------------
    static GameEngine startedEngine(GameConfig cfg) {
        GameEngine e = new GameEngine(cfg);
        check(e.startGame().isOk(), "start");
        return e;
    }
    static Customer waitForWalkIn(GameEngine e) {
        for (int i = 0; i < 2000; i++) {
            for (Customer c : e.getCustomers()) if (c.getAction() == CustomerAction.IN_LINE) return c;
            e.update(0.1);
        }
        throw new AssertionError("no customer arrived");
    }

    // ---------------- data ----------------
    public static void testEveryRecipeIngredientExists() {
        IngredientCatalog cat = new IngredientCatalog();
        for (Dish d : Menu.standard().all()) {
            for (String id : d.getIngredientIds()) check(cat.contains(id), d.getName() + " uses unknown " + id);
            for (String id : d.getPossibleExtras()) check(cat.contains(id), d.getName() + " extra unknown " + id);
        }
    }

    public static void testPolymorphicPrepMethods() {
        IngredientCatalog cat = new IngredientCatalog();
        check(cat.get(IngredientCatalog.NOODLES).getPrepMethod() == PrepMethod.BOIL, "noodles boil");
        check(cat.get(IngredientCatalog.EGG).getPrepMethod() == PrepMethod.CRACK, "egg crack");
        check(cat.get(IngredientCatalog.LIVER).getPrepMethod() == PrepMethod.SLICE, "liver slice");
        check(cat.get(IngredientCatalog.SALT).getCategory() == IngredientCategory.SPICE, "salt spice");
    }

    public static void testLomiOverloadInheritsAndAddsToppings() {
        Dish base = new LomiBatangas(), over = new LomiOverload();
        check(over.getIngredientIds().containsAll(base.getIngredientIds()), "overload has all base ingredients");
        check(over.getIngredientIds().contains(IngredientCatalog.QUAIL_EGG), "overload has quail egg");
        check(over.getPrice() > base.getPrice(), "overload costs more");
        check(over.createSteps(new ArrayList<>()).size() == base.createSteps(new ArrayList<>()).size(), "same steps");
    }

    public static void testInventoryConsumeIsAllOrNothing() {
        Inventory inv = new Inventory(new IngredientCatalog(), 1);
        List<String> two = Arrays.asList("egg", "egg");
        check(inv.missingFor(two).equals(Arrays.asList("egg")), "needs 2 eggs");
        check(!inv.consume(Arrays.asList("noodles", "egg", "egg")), "can't consume");
        check(inv.get("noodles") == 1, "nothing consumed");
    }

    // ---------------- cooking steps ----------------
    public static void testChopStepScoresEvenSwipes() {
        ChopStep good = new ChopStep("onion", "onion", 3), bad = new ChopStep("onion", "onion", 3);
        for (int i = 0; i < 3; i++) { good.handleInput(PlayerInput.swipe(0.6, 1, 0), NO_FEEDBACK); bad.handleInput(PlayerInput.swipe(1.0, 1, 0), NO_FEEDBACK); }
        check(good.isComplete() && bad.isComplete(), "both done");
        check(good.getScore() > bad.getScore(), "even swipes score better");
        ChopStep tiny = new ChopStep("onion", "onion", 1);
        tiny.handleInput(PlayerInput.swipe(0.05, 1, 0), NO_FEEDBACK);
        check(!tiny.isComplete() && tiny.getMistakes() == 1, "tiny swipe doesn't cut");
    }

    public static void testBoilStepBurnsIfIgnored() {
        BoilStep b = new BoilStep("noodles", 2);
        for (int i = 0; i < 50; i++) b.update(0.1, NO_FEEDBACK);
        check(b.isComplete() && b.isBurnt() && b.getScore() < 0.2, "burnt");
        BoilStep ok = new BoilStep("noodles", 2);
        for (int i = 0; i < 20; i++) ok.update(0.1, NO_FEEDBACK);
        ok.handleInput(PlayerInput.tap(), NO_FEEDBACK);
        check(ok.getScore() > 0.95, "on time is ~perfect: " + ok.getScore());
    }

    public static void testFanFireNeedsBlowing() {
        FanFireStep idle = new FanFireStep("boil", 2);
        for (int i = 0; i < 100; i++) idle.update(0.1, NO_FEEDBACK);
        check(!idle.isComplete(), "no blowing, no fire");
        FanFireStep f = new FanFireStep("boil", 2);
        for (int i = 0; i < 200 && !f.isComplete(); i++) {
            f.handleInput(PlayerInput.mic(f.getHeat() < 0.7 ? 0.9 : 0), NO_FEEDBACK);
            f.update(0.1, NO_FEEDBACK);
        }
        check(f.isComplete(), "blowing reaches the zone");
    }

    public static void testFanFireWorksWithSwipesOnly() {
        FanFireStep f = new FanFireStep("boil", 3);
        double t = 0, sinceSwipe = 0;
        for (int i = 0; i < 400 && !f.isComplete(); i++) {
            sinceSwipe += 0.05;
            if (f.getHeat() < 0.7 && sinceSwipe >= 0.4) { f.handleInput(PlayerInput.swipe(0.6, 1, 0), NO_FEEDBACK); sinceSwipe = 0; }
            f.update(0.05, NO_FEEDBACK); t += 0.05;
        }
        check(f.isComplete(), "fanning ~2 swipes/s finishes the stove");
        check(t < 10, "in reasonable time: " + t);
    }

    public static void testAddStepWrongItemIsMistake() {
        AddIngredientsStep a = new AddIngredientsStep("x", Arrays.asList("egg", "noodles"));
        a.handleInput(PlayerInput.drop("tapa"), NO_FEEDBACK);
        a.handleInput(PlayerInput.drop("egg"), NO_FEEDBACK);
        a.handleInput(PlayerInput.drop("noodles"), NO_FEEDBACK);
        check(a.isComplete() && a.getMistakes() == 1, "1 mistake");
        check(Math.abs(a.getScore() - 0.8) < 1e-9, "score 0.8");
    }

    // ---------------- customers ----------------
    public static void testCustomerTypesBehaveDifferently() {
        Menu menu = Menu.standard();
        java.util.Random rng = new java.util.Random(1);
        for (int i = 0; i < 20; i++) {
            StudentCustomer s = new StudentCustomer("s" + i, "Juan");
            s.update(2);
            Order o = s.placeOrder(menu, rng, "o" + i, 0);
            check(o.totalPrice() <= StudentCustomer.BUDGET, "student stays on budget");
        }
        Customer tita = new TitaCustomer("t", "Tita"), reg = new RegularCustomer("r", "Ana");
        double[] tipRatio = new double[2];
        Customer[] both = {tita, reg};
        for (int i = 0; i < 2; i++) {
            both[i].update(2);
            both[i].placeOrder(menu, rng, "x" + i, 0);
            both[i].receiveFood(1.0);
            Payment p = both[i].pay();
            tipRatio[i] = p.getTip() / p.getPrice();
        }
        check(tipRatio[0] > tipRatio[1], "titas tip more");
        check(new JeepneyDriver("d", "Mang Berto").getPatience() < new TitaCustomer("t2", "Tita").getPatience(), "drivers are in a hurry");
    }

    public static void testCustomerLeavesAngryWhenIgnored() {
        GameEngine e = startedEngine(GameConfig.defaults().deliveryChance(0));
        List<EventType> seen = new ArrayList<>();
        e.subscribe(ev -> seen.add(ev.getType()));
        waitForWalkIn(e);
        for (int i = 0; i < 1500; i++) e.update(0.1);
        check(seen.contains(EventType.CUSTOMER_LEFT_ANGRY), "someone left angry");
        check(e.getReport().getCustomersAngry() > 0, "counted");
    }

    // ---------------- full loop ----------------
    public static void testFullDineInLoop() {
        GameEngine e = startedEngine(GameConfig.defaults().deliveryChance(0));
        double money0 = e.wallet().getBalance();
        Customer c = waitForWalkIn(e);
        check(e.takeOrder(c.getId()).isOk(), "take order");
        Order o = c.getOrder();
        check(e.getTaskBoard().hasOpen(CookOrderTask.class), "kitchen note posted");
        check(e.goTo(SceneId.KITCHEN).isOk(), "to kitchen");
        while (o.nextLineToCook() >= 0) {
            check(e.startCooking(o.getId()).isOk(), "cook");
            AutoPlayer.cookCurrentDish(e);
        }
        check(o.getStatus() == OrderStatus.READY, "ready: " + o.getStatus());
        check(o.averageQuality() > 0.8, "good cooking: " + o.averageQuality());
        check(!e.serve(o.getId()).isOk(), "can't serve from kitchen");
        check(e.goTo(SceneId.COUNTER).isOk(), "to counter");
        check(e.serve(o.getId()).isOk(), "serve");
        for (int i = 0; i < 100 && !c.getAction().isGone(); i++) e.update(0.1);
        check(c.getAction() == CustomerAction.LEFT, "paid and left");
        check(c.getRating().getStars() >= 4, "happy customer: " + c.getRating().getStars());
        check(e.wallet().getBalance() >= money0 + o.totalPrice(), "got paid");
        // their table is now dirty -> chore
        e.update(0.1);
        CleanTableTask clean = null;
        for (Task t : e.getTaskBoard().open()) if (t instanceof CleanTableTask) clean = (CleanTableTask) t;
        check(clean != null, "clean-table note posted");
        check(e.goTo(SceneId.MAIN_DINER).isOk(), "to diner");
        check(e.startTask(clean.getId()).isOk(), "start chore");
        AutoPlayer.doChore(e, clean);
        check(clean.getStatus() == TaskStatus.DONE, "table cleaned");
        check(e.shop().getTable(clean.getTableId()).isAvailable(), "table free again");
    }

    public static void testNavigationFollowsTheHub() {
        GameEngine e = startedEngine(GameConfig.defaults());
        check(e.getCurrentScene().getId() == SceneId.COUNTER, "starts at counter");
        check(e.goTo(SceneId.KITCHEN).isOk(), "counter->kitchen");
        check(!e.goTo(SceneId.DELIVERY_HUB).isOk(), "kitchen->hub not direct");
        check(!e.goTo(SceneId.MENU).isOk(), "no menu mid-day");
        check(e.getKaloy().getEmote() == Emote.HAPPY_MASKED, "masked in kitchen");
    }

    public static void testDeliveryLoop() {
        GameEngine e = startedEngine(GameConfig.defaults().deliveryChance(1).spawnInterval(500, 500).startLevel(4));
        for (int i = 0; i < 40 && e.getOrders().isEmpty(); i++) e.update(0.1);
        check(!e.getOrders().isEmpty(), "delivery order arrived");
        Order o = e.getOrders().get(0);
        check(o.isDelivery(), "is delivery");
        e.goTo(SceneId.KITCHEN);
        while (o.nextLineToCook() >= 0) { check(e.startCooking(o.getId()).isOk(), "cook"); AutoPlayer.cookCurrentDish(e); }
        e.goTo(SceneId.COUNTER);
        check(!e.serve(o.getId()).isOk(), "deliveries aren't served at the counter");
        e.goTo(SceneId.DELIVERY_HUB);
        check(e.startDelivery(o.getId()).isOk(), "start run");
        check(!e.goTo(SceneId.COUNTER).isOk(), "can't leave mid-run");
        AutoPlayer.rideDelivery(e);
        check(o.getStatus() == OrderStatus.DELIVERED, "delivered: " + o.getStatus());
        for (int i = 0; i < 20; i++) e.update(0.1);
        check(e.getReport().getDeliveriesDone() == 1, "counted");
        check(e.getReport().getRevenue() > 0, "paid");
    }

    public static void testOutOfStockThenRestock() {
        GameConfig cfg = GameConfig.defaults().deliveryChance(0);
        cfg.startingStock = 0;
        cfg.morningSupplier = false;
        GameEngine e = startedEngine(cfg);
        Customer c = waitForWalkIn(e);
        e.takeOrder(c.getId());
        e.goTo(SceneId.KITCHEN);
        check(!e.startCooking(null).isOk(), "nothing in the pantry");
        e.update(0.1);
        check(e.getTaskBoard().hasOpen(RestockTask.class), "restock notes posted");
        e.goTo(SceneId.COUNTER); e.goTo(SceneId.DELIVERY_HUB);
        double before = e.wallet().getBalance();
        check(e.buy("egg", 10).isOk(), "buy eggs");
        check(e.inventory().get("egg") == 10, "eggs in pantry");
        check(e.wallet().getBalance() < before, "paid for eggs");
        check(!e.buy("egg", 100000).isOk(), "can't overspend");
    }

    public static void testDayEnds() {
        GameEngine e = startedEngine(GameConfig.defaults().dayLength(20).deliveryChance(0).spawnInterval(1000, 1000));
        for (int i = 0; i < 3000 && e.getState() == GameState.PLAYING; i++) e.update(0.1);
        check(e.getState() == GameState.DAY_OVER, "day over");
        check(e.nextDay().isOk() && e.getDay() == 2, "day 2");
    }

    // ---------------- progression, upgrades, staff, saves ----------------
    public static void testStartsSmall() {
        GameEngine e = startedEngine(GameConfig.defaults());
        check(e.getProgress().getLevel() == 1, "level 1");
        check(e.getMenu().all().size() == 1 && e.getMenu().has("lomi_batangas"), "only Lomi Batangas at first");
        check(e.shop().getTables().size() == 2, "2 tables");
        check(e.getProgress().getGoals().size() == 3, "3 daily goals");
        check(e.inventory().get("noodles") >= 6, "supplier stocked the pantry");
    }

    public static void testLevelUpUnlocksDishes() {
        com.lomiking.core.progress.Progression p = new com.lomiking.core.progress.Progression();
        List<Integer> ups = p.addXp(com.lomiking.core.progress.Unlocks.xpToNext(1) + 5);
        check(ups.size() == 1 && p.getLevel() == 2, "level 2");
        check(com.lomiking.core.progress.Unlocks.unlockedDishes(2).contains("lumpiang_shanghai"), "shanghai at level 2");
        check(!com.lomiking.core.progress.Unlocks.unlockedDishes(3).contains("tapsilog"), "tapsilog later");
    }

    public static void testStreakBonus() {
        com.lomiking.core.progress.Progression p = new com.lomiking.core.progress.Progression();
        p.cooked(3); p.cooked(3); p.cooked(3);
        check(p.getStreak() == 3 && Math.abs(p.streakTipBonus() - 0.2) < 1e-9, "streak 3 gives +20% tips");
        p.cooked(2);
        check(p.getStreak() == 0, "a 2-star dish breaks the streak");
    }

    public static void testUpgradesNeedLevelAndMoney() {
        GameConfig cfg = GameConfig.defaults().startLevel(2); cfg.startingMoney = 2000;
        GameEngine e = startedEngine(cfg);
        check(e.buyUpgrade("tables").isOk(), "buy a table");
        check(e.shop().getTables().size() == 3, "3 tables now");
        check(!e.buyUpgrade("knife").isOk(), "knife needs level 3");
        GameConfig poor = GameConfig.defaults().startLevel(10); poor.startingMoney = 0; poor.morningSupplier = false;
        check(!startedEngine(poor).buyUpgrade("tables").isOk(), "can't buy without money");
    }

    public static void testKnifeUpgradeMeansFewerCuts() {
        java.util.List<CookingStep> base = new LomiBatangas().createSteps(new ArrayList<>(), KitchenPerks.NONE);
        java.util.List<CookingStep> sharp = new LomiBatangas().createSteps(new ArrayList<>(), new KitchenPerks(2, 0, 0));
        check(base.get(0).viewState().get("cutsNeeded").equals(3), "3 cuts normally");
        check(sharp.get(0).viewState().get("cutsNeeded").equals(1), "1 cut with a level-2 knife");
    }

    public static void testHiredStaffRunTheShop() {
        GameConfig cfg = GameConfig.defaults().startLevel(9).deliveryChance(0); cfg.startingMoney = 5000;
        GameEngine e = startedEngine(cfg);
        check(!e.hireWorker("nobody").isOk(), "unknown helper");
        check(e.hireWorker("server").isOk() && e.hireWorker("cook").isOk() && e.hireWorker("cleaner").isOk(), "hire 3 helpers");
        for (int i = 0; i < 2000; i++) e.update(0.1);    // the player does nothing at all
        check(e.getReport().getCustomersServed() >= 3, "helpers served customers: " + e.getReport().getCustomersServed());
        check(e.getReport().getRevenue() > 0, "and got paid");
    }

    public static void testSaveAndContinue() {
        GameConfig cfg = GameConfig.defaults().startLevel(5); cfg.startingMoney = 3000;
        GameEngine e = startedEngine(cfg);
        e.buyUpgrade("tables"); e.buyUpgrade("decor"); e.hireWorker("server"); e.trainWorker("server");
        double money = e.wallet().getBalance();
        String save = e.exportSave();
        GameEngine e2 = new GameEngine();
        check(e2.importSave(save).isOk(), "loads");
        check(e2.getProgress().getLevel() == 5, "level restored");
        check(e2.getUpgrades().level("decor") == 1 && e2.shop().getTables().size() == 3, "upgrades restored");
        check(e2.getStaff().get("server").getLevel() == 2, "staff restored");
        check(Math.abs(e2.wallet().getBalance() - money) < 0.01, "money restored");
        check(e2.startGame().isOk() && e2.getMenu().has("tapsilog"), "continues with unlocked menu");
        check(!e2.importSave(save).isOk(), "can't load mid-game");
    }

    public static void testGoalsPayRewards() {
        GameEngine e = startedEngine(GameConfig.defaults().deliveryChance(0));
        double before = e.wallet().getBalance();
        int served = 0;
        for (int i = 0; i < 6000 && e.getState() == GameState.PLAYING; i++) {
            for (Customer c : e.getCustomers()) if (c.getAction() == CustomerAction.IN_LINE) { e.goTo(SceneId.COUNTER); e.takeOrder(c.getId()); }
            for (Order o : e.getOrders()) if (o.isOpen() && o.nextLineToCook() >= 0) { e.goTo(SceneId.COUNTER); e.goTo(SceneId.KITCHEN); if (e.startCooking(o.getId()).isOk()) AutoPlayer.cookCurrentDish(e); }
            for (Order o : e.getOrders()) if (o.getStatus() == OrderStatus.READY) { e.goTo(SceneId.COUNTER); if (e.serve(o.getId()).isOk()) served++; }
            for (Task t : e.getTaskBoard().open()) if (t.isInteractive()) { e.goTo(SceneId.COUNTER); e.goTo(t.getSector().getScene()); if (e.startTask(t.getId()).isOk()) AutoPlayer.doChore(e, t); }
            e.update(0.1);
        }
        check(served >= 4, "served a few: " + served);
        check(e.getProgress().getLevel() >= 2 || e.getProgress().getXp() > 50, "gained XP/levels");
        check(e.getProgress().goalsDone() >= 1, "completed a daily goal");
        check(e.getReport().getBonuses() > 0, "goal/achievement bonuses paid");
    }

    // ---------------- bridge ----------------
    @SuppressWarnings("unchecked")
    public static void testBridgeJsonRoundTrip() {
        GameBridge b = new GameBridge();
        Map<String, Object> r = Json.parseObject(b.dispatch("{\"cmd\":\"start\"}"));
        check(Boolean.TRUE.equals(r.get("ok")), "start ok");
        r = Json.parseObject(b.dispatch("{\"cmd\":\"goTo\",\"scene\":\"KITCHEN\"}"));
        check(Boolean.TRUE.equals(r.get("ok")), "goTo ok");
        r = Json.parseObject(b.dispatch("{\"cmd\":\"fly\"}"));
        check(Boolean.FALSE.equals(r.get("ok")), "unknown cmd rejected");
        for (int i = 0; i < 100; i++) b.tick(0.05);
        Map<String, Object> snap = Json.parseObject(b.snapshot());
        check("PLAYING".equals(snap.get("state")), "playing");
        check("KITCHEN".equals(((Map<String, Object>) snap.get("scene")).get("id")), "in kitchen");
        check("HAPPY_MASKED".equals(((Map<String, Object>) snap.get("kaloy")).get("emote")), "masked");
        List<Object> events = (List<Object>) Json.parse(b.drainEvents());
        check(!events.isEmpty(), "events flowed");
        check(((List<Object>) Json.parse(b.drainEvents())).isEmpty(), "drained");
        Map<String, Object> cat = Json.parseObject(b.catalog());
        check(((List<Object>) cat.get("menu")).size() == 5, "5 dishes");
        snap = Json.parseObject(b.snapshot());
        check(snap.get("progress") instanceof Map && snap.get("upgrades") instanceof List && snap.get("staff") instanceof List, "progression in snapshot");
        check(((List<Object>) snap.get("goals")).size() == 3, "goals in snapshot");
        String save = b.exportSave();
        check(save.contains("\"progress\""), "save exported");
    }

    public static void testJsonEscapes() {
        String s = Json.stringify(java.util.Collections.singletonMap("say", "Iho, \"malapot\"\nha!"));
        check(Json.parseObject(s).get("say").equals("Iho, \"malapot\"\nha!"), "round trip escapes");
    }
}
