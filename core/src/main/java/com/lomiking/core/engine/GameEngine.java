package com.lomiking.core.engine;

import com.lomiking.core.character.Emote;
import com.lomiking.core.character.MangKaloy;
import com.lomiking.core.command.CommandResult;
import com.lomiking.core.cooking.CookedDish;
import com.lomiking.core.cooking.CookingFeedback;
import com.lomiking.core.cooking.CookingSession;
import com.lomiking.core.cooking.CookingStep;
import com.lomiking.core.cooking.PlayerInput;
import com.lomiking.core.customer.Customer;
import com.lomiking.core.customer.CustomerAction;
import com.lomiking.core.customer.CustomerFactory;
import com.lomiking.core.customer.DeliveryCustomer;
import com.lomiking.core.customer.Emotion;
import com.lomiking.core.customer.Order;
import com.lomiking.core.customer.OrderLine;
import com.lomiking.core.customer.OrderStatus;
import com.lomiking.core.customer.Payment;
import com.lomiking.core.customer.Rating;
import com.lomiking.core.delivery.DeliveryMiniGame;
import com.lomiking.core.delivery.Obstacle;
import com.lomiking.core.economy.DayReport;
import com.lomiking.core.economy.Inventory;
import com.lomiking.core.economy.Market;
import com.lomiking.core.economy.Wallet;
import com.lomiking.core.event.EventBus;
import com.lomiking.core.event.EventType;
import com.lomiking.core.event.GameEventListener;
import com.lomiking.core.food.IngredientCatalog;
import com.lomiking.core.food.Menu;
import com.lomiking.core.scene.*;
import com.lomiking.core.shop.Shop;
import com.lomiking.core.shop.Table;
import com.lomiking.core.cooking.KitchenPerks;
import com.lomiking.core.food.Dish;
import com.lomiking.core.progress.Achievement;
import com.lomiking.core.progress.DailyGoal;
import com.lomiking.core.progress.Progression;
import com.lomiking.core.progress.Unlocks;
import com.lomiking.core.staff.CookWorker;
import com.lomiking.core.staff.RiderWorker;
import com.lomiking.core.staff.Staff;
import com.lomiking.core.staff.StaffContext;
import com.lomiking.core.staff.Worker;
import com.lomiking.core.upgrade.Upgrade;
import com.lomiking.core.upgrade.UpgradeShop;
import com.lomiking.core.bridge.Json;
import com.lomiking.core.task.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * The whole game world and its rules. Has no rendering, sound or Android code: the view layer calls
 * update(dt) every frame, sends player actions through the public methods (or JSON via GameBridge),
 * and draws whatever state and events come back.
 */
public class GameEngine implements GameContext, StaffContext {
    private final GameConfig config;
    private final Random rng;
    private final EventBus events;
    private final Menu fullMenu = Menu.standard();
    private Menu menu;                                  // only the dishes unlocked so far
    private final Progression progress = new Progression();
    private final UpgradeShop upgrades = new UpgradeShop();
    private final Staff staff = new Staff();
    private final IngredientCatalog catalog = new IngredientCatalog();
    private final Inventory inventory;
    private final Wallet wallet;
    private final Market market;
    private final Shop shop;
    private final TaskBoard taskBoard = new TaskBoard();
    private final MangKaloy kaloy = new MangKaloy();
    private final CustomerFactory customerFactory;
    private final Map<SceneId, Scene> scenes = new EnumMap<>(SceneId.class);
    private final List<Customer> customers = new ArrayList<>();
    private final Map<String, Order> orders = new LinkedHashMap<>();
    private final Map<String, Emotion> lastEmotion = new LinkedHashMap<>();

    private GameState state = GameState.MENU;
    private Scene currentScene;
    private double time, spawnTimer, reputation;
    private int day, orderCounter;
    private boolean closed;
    private DayReport report = new DayReport(0);

    private final CookingFeedback cookingFeedback = new CookingFeedback() {
        @Override public void mistake(String reason) { emit(EventType.COOKING_MISTAKE, "reason", reason); react(Emote.NERVOUS, 1.0); }
        @Override public void burnt(String what) { emit(EventType.FOOD_BURNT, "what", what); }
        @Override public void emote(Emote e, double seconds) { react(e, seconds); }
    };

    private final CookingSession.Listener cookingListener = new CookingSession.Listener() {
        @Override public void stepStarted(CookingSession s, CookingStep step) {
            emit(EventType.COOKING_STEP_STARTED, "orderId", s.getOrderId(), "dish", s.getDish().getId(),
                 "index", s.getStepIndex(), "step", step.getClass().getSimpleName(), "title", step.getTitle(), "gesture", step.getGesture().name());
        }
        @Override public void stepCompleted(CookingSession s, CookingStep step) {
            emit(EventType.COOKING_STEP_COMPLETED, "orderId", s.getOrderId(), "index", s.getStepIndex(), "score", round2(step.getScore()));
        }
        @Override public void finished(CookingSession s, CookedDish result) { onDishCooked(s, result); }
    };

    public GameEngine(GameConfig config) {
        this.config = config;
        this.rng = new Random(config.seed);
        this.events = new EventBus(() -> time);
        this.inventory = new Inventory(catalog, config.startingStock);
        this.wallet = new Wallet(config.startingMoney);
        this.market = new Market(catalog);
        this.shop = new Shop(config.tableCount);
        progress.setLevel(config.startLevel);
        refreshMenu();
        this.customerFactory = new CustomerFactory(rng);
        this.reputation = config.startingReputation;
        for (Scene s : new Scene[]{new MenuScene(), new CounterScene(), new KitchenScene(), new MainDinerScene(), new DeliveryHubScene()})
            scenes.put(s.getId(), s);
        currentScene = scenes.get(SceneId.MENU);
    }

    public GameEngine() { this(GameConfig.defaults()); }

    // =====================================================================================
    // Game flow
    // =====================================================================================

    /** The Start button on the menu. */
    public CommandResult startGame() {
        if (state != GameState.MENU) return reject("The game already started");
        state = GameState.PLAYING;
        emit(EventType.GAME_STARTED);
        beginDay(Math.max(1, day == 0 ? 1 : day));
        return CommandResult.ok("Bukas na ang Lomihan!");
    }

    public CommandResult nextDay() {
        if (state != GameState.DAY_OVER) return reject("The day isn't over yet");
        state = GameState.PLAYING;
        beginDay(day + 1);
        return CommandResult.ok("Day " + day);
    }

    public CommandResult pause() {
        if (state != GameState.PLAYING) return reject("Not playing");
        state = GameState.PAUSED; emit(EventType.PAUSED);
        return CommandResult.ok("Paused");
    }

    public CommandResult resume() {
        if (state != GameState.PAUSED) return reject("Not paused");
        state = GameState.PLAYING; emit(EventType.RESUMED);
        return CommandResult.ok("Resumed");
    }

    private void beginDay(int d) {
        day = d;
        time = 0;
        closed = false;
        spawnTimer = config.firstCustomerDelay;
        customers.clear();
        orders.clear();
        lastEmotion.clear();
        taskBoard.clearFinished();
        report = new DayReport(d);
        switchScene(SceneId.COUNTER);
        morningSupplier();
        progress.newGoals(rng, deliveriesOpen());
        emit(EventType.DAY_STARTED, "day", d, "money", wallet.getBalance(), "level", progress.getLevel());
    }

    /** The supplier tops up every ingredient the unlocked dishes use, at a small discount. */
    private void morningSupplier() {
        if (!config.morningSupplier) return;
        int lvl = upgrades.level(UpgradeShop.SUPPLIER);
        int target = 6 + 3 * lvl;
        double discount = 0.9 - 0.05 * lvl;
        java.util.Set<String> needed = new java.util.LinkedHashSet<>();
        for (Dish d : menu.all()) { needed.addAll(d.getIngredientIds()); needed.addAll(d.getPossibleExtras()); }
        double bill = 0; int items = 0;
        for (String id : needed) {
            int missing = target - inventory.get(id);
            if (missing <= 0) continue;
            double price = market.priceOf(id, missing) * discount;
            if (!wallet.canAfford(bill + price)) continue;
            bill += price; items += missing;
            inventory.add(id, missing);
        }
        bill = Math.round(bill);
        if (bill > 0) { wallet.spend(bill); report.recordSpent(bill); }
        emit(EventType.SUPPLIER_DELIVERY, "items", items, "bill", bill);
        emitMoney();
    }

    private boolean deliveriesOpen() { return progress.getLevel() >= Unlocks.DELIVERY_LEVEL; }

    private void refreshMenu() {
        menu = fullMenu.only(Unlocks.unlockedDishes(progress.getLevel()));
    }

    private KitchenPerks perks() {
        return new KitchenPerks(upgrades.level(UpgradeShop.KNIFE), upgrades.level(UpgradeShop.STOVE), upgrades.level(UpgradeShop.RECIPE));
    }

    /** Advance the simulation. Call every frame with the frame time in seconds. */
    public void update(double dt) {
        if (state != GameState.PLAYING || dt <= 0) return;
        dt = Math.min(dt, 0.25);   // don't let a long frame (app in background) skip whole events
        time += dt;
        if (kaloy.update(dt)) emitEmote();
        if (!closed && time >= config.dayLengthSeconds) closed = true;
        if (!closed) spawnCustomers(dt);
        updateCustomers(dt);
        currentScene.update(dt, this);
        updateDelivery();
        staff.update(dt, this);
        if (!customers.isEmpty()) shop.addFloorDirt(dt * 0.002);
        postChores();
        for (Task t : taskBoard.update(dt, this)) onTaskFinished(t);
        checkGoals();
        RiderWorker rider = (RiderWorker) staff.get("rider");
        if (closed && customers.isEmpty() && delivery().getRun() == null && !rider.isOut()) endDay();
    }

    private void endDay() {
        state = GameState.DAY_OVER;
        double wages = staff.totalWages();
        double paid = Math.min(wages, wallet.getBalance());
        if (paid > 0) { wallet.spend(paid); report.recordWages(paid); emit(EventType.WAGES_PAID, "amount", paid); emitMoney(); }
        emit(EventType.DAY_ENDED, "day", day, "goalsDone", progress.goalsDone(), "wages", paid, "served", report.getCustomersServed(), "angry", report.getCustomersAngry(),
             "revenue", report.getRevenue(), "tips", report.getTips(), "spent", report.getSpent(), "profit", report.getProfit(),
             "avgRating", round2(report.getAverageRating()), "reputation", round2(reputation));
    }

    // =====================================================================================
    // Navigation (hub and spoke around the counter, see docs/game_flow.png)
    // =====================================================================================

    public CommandResult goTo(SceneId target) {
        if (state != GameState.PLAYING) return reject("Can't move right now");
        if (target == currentScene.getId()) return CommandResult.ok("Already here");
        String block = currentScene.blockLeavingReason();
        if (block != null) return reject(block);
        if (!currentScene.canGoTo(target)) return reject("Can't go from " + currentScene.getId() + " to " + target + " directly");
        switchScene(target);
        return CommandResult.ok("Now in " + target);
    }

    private void switchScene(SceneId target) {
        SceneId from = currentScene.getId();
        currentScene.onExit(this);
        currentScene = scenes.get(target);
        currentScene.onEnter(this);
        if (kaloy.setRestingEmote(currentScene.getRestingEmote())) emitEmote();
        emit(EventType.SCENE_CHANGED, "from", from.name(), "to", target.name(), "environment", currentScene.getEnvironmentModel());
    }

    // =====================================================================================
    // Customers
    // =====================================================================================

    private void spawnCustomers(double dt) {
        spawnTimer -= dt;
        if (spawnTimer > 0) return;
        double repFactor = Math.max(0.6, 1.2 - 0.1 * (reputation - 3));
        double tableFactor = Math.sqrt(2.0 / Math.max(2, shop.getTables().size()));
        double signFactor = 1 - 0.12 * upgrades.level(UpgradeShop.SIGN);
        spawnTimer = (config.spawnMinSeconds + rng.nextDouble() * (config.spawnMaxSeconds - config.spawnMinSeconds)) * repFactor * tableFactor * signFactor;
        int queueCap = Math.min(config.maxQueue, shop.getTables().size() + 1);

        if (deliveriesOpen() && rng.nextDouble() < config.deliveryChance) {
            DeliveryCustomer c = customerFactory.createDelivery();
            c.scalePatience(patienceFactor());
            customers.add(c);
            Order o = c.placeOrder(menu, rng, nextOrderId(), time);
            orders.put(o.getId(), o);
            post(new CookOrderTask(taskBoard.nextId(), o));
            post(new DeliveryTask(taskBoard.nextId(), o, c.getAddress()));
            emit(EventType.CUSTOMER_ARRIVED, "customerId", c.getId(), "type", c.getTypeLabel(), "name", c.getName(), "model", c.getModelId(), "remote", true);
            emit(EventType.ORDER_CREATED, "orderId", o.getId(), "customerId", c.getId(), "summary", o.summary(), "delivery", true);
        } else if (countInLine() < queueCap) {
            Customer c = customerFactory.createWalkIn(reputation);
            c.scalePatience(patienceFactor());
            customers.add(c);
            emit(EventType.CUSTOMER_ARRIVED, "customerId", c.getId(), "type", c.getTypeLabel(), "name", c.getName(), "model", c.getModelId(), "remote", false);
        }
    }

    private double patienceFactor() { return config.patienceMultiplier * (1 + 0.15 * upgrades.level(UpgradeShop.DECOR)); }

    private int countInLine() {
        int n = 0;
        for (Customer c : customers) if (c.getAction() == CustomerAction.ARRIVING || c.getAction() == CustomerAction.IN_LINE) n++;
        return n;
    }

    private void updateCustomers(double dt) {
        for (Customer c : new ArrayList<>(customers)) {
            CustomerAction before = c.getAction();
            c.update(dt);
            CustomerAction after = c.getAction();
            if (after == CustomerAction.LEFT_ANGRY && before != CustomerAction.LEFT_ANGRY) onCustomerAngry(c);
            if (after == CustomerAction.PAYING) onCustomerPays(c);
            Emotion e = c.getEmotion();
            if (e != lastEmotion.get(c.getId())) {
                lastEmotion.put(c.getId(), e);
                emit(EventType.CUSTOMER_EMOTION_CHANGED, "customerId", c.getId(), "emotion", e.name());
            }
            if (c.getAction().isGone()) {
                customers.remove(c);
                lastEmotion.remove(c.getId());
                emit(EventType.CUSTOMER_LEFT, "customerId", c.getId(), "angry", c.getAction() == CustomerAction.LEFT_ANGRY);
            }
        }
    }

    private void onCustomerAngry(Customer c) {
        if (c.getTableId() > 0) shop.vacate(c.getTableId(), 0.15, 0);
        Order o = c.getOrder();
        if (o != null && o.isOpen()) o.cancel();
        report.recordAngry();
        reputation = reputation * 0.9 + 1 * 0.1;
        react(Emote.UH_OH, 2.0);
        emit(EventType.CUSTOMER_LEFT_ANGRY, "customerId", c.getId(), "name", c.getName());
    }

    private void onCustomerPays(Customer c) {
        Rating r = c.rate();
        Payment p = c.pay();
        double bonusTip = Math.round(p.getPrice() * (0.05 * upgrades.level(UpgradeShop.DECOR) + progress.streakTipBonus()) * (r.getStars() >= 3 ? 1 : 0));
        wallet.earn(p.getTotal() + bonusTip);
        report.recordSale(p.getPrice(), p.getTip() + bonusTip, r.getStars());
        progress.served(r.getStars(), p.getTotal() + bonusTip);
        if (c.isRemote()) { progress.delivered(); }
        gainXp(8 + 3 * r.getStars() + (c.isRemote() ? 10 : 0), c.isRemote() ? "Delivery" : "Happy customer");
        checkAchievements();
        reputation = reputation * 0.9 + r.getStars() * 0.1;
        if (c.getTableId() > 0) shop.vacate(c.getTableId(), 0.4 + 0.15 * c.getOrder().getLines().size(), c.getOrder().getLines().size());
        react(r.getStars() >= 5 ? Emote.LAUGH : r.getStars() <= 2 ? Emote.NERVOUS : Emote.HAPPY, 2.0);
        emit(EventType.CUSTOMER_PAID, "customerId", c.getId(), "stars", r.getStars(), "comment", r.getComment(),
             "price", p.getPrice(), "tip", p.getTip() + bonusTip, "bonusTip", bonusTip);
        emitMoney();
    }

    /** At the counter: take the order of the customer at the front (or a specific one). */
    public CommandResult takeOrder(String customerId) {
        if (state != GameState.PLAYING) return reject("Not playing");
        if (currentScene.getId() != SceneId.COUNTER) return reject("Go to the counter to take orders");
        Customer c = customerId == null ? firstInLine() : findCustomer(customerId);
        if (c == null) return reject("No customer waiting");
        if (c.getAction() != CustomerAction.IN_LINE) return reject(c.getName() + " isn't in line");
        return doTakeOrder(c);
    }

    private CommandResult doTakeOrder(Customer c) {
        Table table = shop.firstAvailableTable();
        if (table == null) return reject("No clean table free. Wipe a table first!");
        shop.seat(c.getId());
        c.seatAt(table.getId());
        Order o = c.placeOrder(menu, rng, nextOrderId(), time);
        orders.put(o.getId(), o);
        post(new CookOrderTask(taskBoard.nextId(), o));
        emit(EventType.CUSTOMER_ORDERED, "customerId", c.getId(), "orderId", o.getId(), "say", c.greeting(),
             "summary", o.summary(), "tableId", table.getId());
        emit(EventType.ORDER_CREATED, "orderId", o.getId(), "customerId", c.getId(), "summary", o.summary(), "delivery", false);
        return CommandResult.ok(c.getName() + ": " + o.summary());
    }

    private Customer firstInLine() {
        for (Customer c : customers) if (c.getAction() == CustomerAction.IN_LINE) return c;
        return null;
    }

    /** At the counter: hand a ready order to its seated customer. */
    public CommandResult serve(String orderId) {
        if (state != GameState.PLAYING) return reject("Not playing");
        if (currentScene.getId() != SceneId.COUNTER) return reject("Serve from the counter");
        Order o = orderId == null ? firstReadyOrder(false) : orders.get(orderId);
        if (o == null) return reject("No order ready to serve");
        if (o.isDelivery()) return reject("That's a delivery order. Take it to the delivery hub");
        if (o.getStatus() != OrderStatus.READY) return reject("Order " + o.getId() + " is " + o.getStatus());
        return doServe(o);
    }

    private CommandResult doServe(Order o) {
        Customer c = findCustomer(o.getCustomerId());
        if (c == null || c.getAction() != CustomerAction.WAITING_FOR_FOOD) return reject("The customer isn't waiting anymore");
        o.markServed();
        c.receiveFood(o.averageQuality());
        report.recordServed();
        emit(EventType.CUSTOMER_SERVED, "customerId", c.getId(), "orderId", o.getId(), "quality", round2(o.averageQuality()), "tableId", c.getTableId());
        return CommandResult.ok("Served " + c.getName());
    }

    private Order firstReadyOrder(boolean delivery) {
        for (Order o : orders.values()) if (o.getStatus() == OrderStatus.READY && o.isDelivery() == delivery) return o;
        return null;
    }

    // =====================================================================================
    // Kitchen
    // =====================================================================================

    /** In the kitchen: start cooking the next dish of an order (null = oldest order waiting). */
    public CommandResult startCooking(String orderId) {
        if (state != GameState.PLAYING) return reject("Not playing");
        if (currentScene.getId() != SceneId.KITCHEN) return reject("Go to the kitchen first");
        KitchenScene k = kitchen();
        if (k.getSession() != null) return reject("Finish the dish on the stove first");
        Order o = orderId == null ? firstOrderToCook() : orders.get(orderId);
        if (o == null) return reject("No orders to cook");
        if (!o.isOpen()) return reject("Order " + o.getId() + " is " + o.getStatus());
        int line = o.nextLineToCook();
        if (line < 0) return reject("Everything in " + o.getId() + " is already cooked");
        OrderLine ol = o.getLines().get(line);
        List<String> needed = new ArrayList<>(ol.getDish().getIngredientIds());
        needed.addAll(ol.getExtras());
        List<String> missing = inventory.missingFor(needed);
        if (!missing.isEmpty()) {
            for (String id : missing) postRestock(id);
            emit(EventType.OUT_OF_STOCK, "missing", String.join(",", missing));
            react(Emote.UH_OH, 1.5);
            return reject("Out of " + String.join(", ", missing) + ". Buy more at the delivery hub");
        }
        inventory.consume(needed);
        o.startCooking(line);
        CookingSession s = new CookingSession(o.getId(), line, ol.getDish(), ol.getExtras(), perks());
        k.setSession(s);
        emit(EventType.COOKING_STARTED, "orderId", o.getId(), "dish", ol.getDish().getId(), "steps", s.getStepCount());
        s.begin(cookingListener);
        return CommandResult.ok("Cooking " + ol.getDish().getName());
    }

    private Order firstOrderToCook() {
        for (Order o : orders.values()) if (o.isOpen() && o.nextLineToCook() >= 0) return o;
        return null;
    }

    private void onDishCooked(CookingSession s, CookedDish result) {
        kitchen().clearSession();
        report.recordCooked();
        Order o = orders.get(s.getOrderId());
        emit(EventType.COOKING_FINISHED, "orderId", s.getOrderId(), "dish", s.getDish().getId(),
             "quality", round2(result.getQuality()), "stars", result.getCookStars(), "burnt", result.isBurnt());
        react(result.getCookStars() == 3 ? Emote.LAUGH : result.getCookStars() == 1 ? Emote.NERVOUS : Emote.HAPPY, 2.0);
        int before = progress.getStreak();
        int streak = progress.cooked(result.getCookStars());
        if (result.getCookStars() >= 3) {
            report.recordPerfectDish();
            gainXp(5, "Perfect dish");
            if (streak >= 2) emit(EventType.STREAK, "streak", streak, "bonus", progress.streakTipBonus());
        } else if (before >= 2) emit(EventType.STREAK_BROKEN, "was", before);
        checkAchievements();
        if (o == null || o.getStatus() == OrderStatus.CANCELLED) return;   // customer left meanwhile: wasted
        completeLine(o, s.getLineIndex(), result);
    }

    private void completeLine(Order o, int line, CookedDish result) {
        o.finishCooking(line, result);
        if (o.getStatus() == OrderStatus.READY)
            emit(EventType.ORDER_READY, "orderId", o.getId(), "delivery", o.isDelivery(), "quality", round2(o.averageQuality()));
    }

    // =====================================================================================
    // Player gestures
    // =====================================================================================

    /** Any touch/mic gesture; the current room decides what it means. */
    public CommandResult input(PlayerInput in) {
        if (state != GameState.PLAYING) return reject("Not playing");
        if (!currentScene.handleInput(in, this)) return reject("Nothing to do with " + in.getType() + " here");
        return CommandResult.ok("");
    }

    // =====================================================================================
    // Chores (tasks / hanging notes)
    // =====================================================================================

    public CommandResult startTask(String taskId) {
        if (state != GameState.PLAYING) return reject("Not playing");
        Task t = taskBoard.get(taskId);
        if (t == null || t.isFinished()) return reject("No open note " + taskId);
        if (!t.isInteractive()) return reject("That note finishes by itself: " + t.getTitle());
        if (t.getSector().getScene() != currentScene.getId()) return reject("That chore is in " + t.getSector());
        diner().setActiveTask(t);
        emit(EventType.TASK_STARTED, "taskId", t.getId(), "type", t.getClass().getSimpleName());
        return CommandResult.ok("Working on: " + t.getTitle());
    }

    private void postChores() {
        for (Table t : shop.getTables()) if (!t.isClean() && t.isFree() && !hasCleanTask(t.getId())) {
            emit(EventType.TABLE_DIRTY, "tableId", t.getId());
            post(new CleanTableTask(taskBoard.nextId(), t.getId()));
        }
        if (shop.getDirtyDishes() >= config.dishPileForTask && !taskBoard.hasOpen(WashDishesTask.class))
            post(new WashDishesTask(taskBoard.nextId(), shop.getDirtyDishes()));
        if (shop.getFloorDirt() >= config.floorDirtForTask && !taskBoard.hasOpen(SweepFloorTask.class))
            post(new SweepFloorTask(taskBoard.nextId()));
        for (String id : inventory.lowStock(config.lowStockThreshold)) postRestock(id);
    }

    private boolean hasCleanTask(int tableId) {
        for (Task t : taskBoard.open()) if (t instanceof CleanTableTask && ((CleanTableTask) t).getTableId() == tableId) return true;
        return false;
    }

    private void postRestock(String ingredientId) {
        for (Task t : taskBoard.open()) if (t instanceof RestockTask && ((RestockTask) t).getIngredientId().equals(ingredientId)) return;
        int target = Math.max(config.restockTarget, inventory.get(ingredientId) + 1);
        post(new RestockTask(taskBoard.nextId(), ingredientId, catalog.get(ingredientId).getDisplayName(), target));
        emit(EventType.LOW_STOCK, "ingredientId", ingredientId, "have", inventory.get(ingredientId));
    }

    private void post(Task t) {
        taskBoard.post(t);
        emit(EventType.TASK_POSTED, "taskId", t.getId(), "type", t.getClass().getSimpleName(), "title", t.getTitle(), "sector", t.getSector().name());
    }

    private void onTaskFinished(Task t) {
        diner().clearIfActive(t);
        if (t.getStatus() == TaskStatus.DONE) {
            if (t.getRewardMoney() > 0) { wallet.earn(t.getRewardMoney()); emitMoney(); }
            report.recordTask(true);
            if (t.isInteractive() || t instanceof RestockTask) { report.recordChore(); gainXp(3, "Chore done"); }
            if (t instanceof CleanTableTask) emit(EventType.TABLE_CLEANED, "tableId", ((CleanTableTask) t).getTableId());
            emit(EventType.TASK_COMPLETED, "taskId", t.getId(), "title", t.getTitle(), "reward", t.getRewardMoney());
        } else {
            report.recordTask(false);
            emit(EventType.TASK_EXPIRED, "taskId", t.getId(), "title", t.getTitle());
        }
    }

    // =====================================================================================
    // Delivery hub: market + delivery runs
    // =====================================================================================

    public CommandResult buy(String ingredientId, int qty) {
        if (state != GameState.PLAYING) return reject("Not playing");
        if (currentScene.getId() != SceneId.DELIVERY_HUB) return reject("The market is reached from the delivery hub");
        if (!catalog.contains(ingredientId)) return reject("Unknown ingredient " + ingredientId);
        if (qty <= 0) return reject("Buy at least 1");
        double paid = market.buy(ingredientId, qty, wallet, inventory);
        if (paid < 0) return reject("Not enough money (needs P" + market.priceOf(ingredientId, qty) + ")");
        report.recordSpent(paid);
        emit(EventType.ITEM_BOUGHT, "ingredientId", ingredientId, "qty", qty, "paid", paid);
        emitMoney();
        return CommandResult.ok("Bought " + qty + " " + ingredientId + " for P" + paid);
    }

    public CommandResult startDelivery(String orderId) {
        if (state != GameState.PLAYING) return reject("Not playing");
        if (currentScene.getId() != SceneId.DELIVERY_HUB) return reject("Go to the delivery hub");
        DeliveryHubScene hub = delivery();
        if (hub.getRun() != null) return reject("Already on a delivery");
        Order o = orderId == null ? firstReadyOrder(true) : orders.get(orderId);
        if (o == null) return reject("No delivery order is ready");
        if (!o.isDelivery()) return reject("That order is for a customer inside");
        if (o.getStatus() != OrderStatus.READY) return reject("Order " + o.getId() + " is " + o.getStatus());
        o.markOutForDelivery();
        int sc = upgrades.level(UpgradeShop.SCOOTER);
        hub.setRun(new DeliveryMiniGame(o.getId(), config.deliveryDistance, o.maxFragility(), rng, 1 + 0.15 * sc, Math.max(0.3, 1 - 0.2 * sc)));
        emit(EventType.DELIVERY_STARTED, "orderId", o.getId(), "environment", hub.getEnvironmentModel());
        return CommandResult.ok("Off we go!");
    }

    private void updateDelivery() {
        DeliveryHubScene hub = delivery();
        DeliveryMiniGame run = hub.getRun();
        if (run == null) return;
        for (Obstacle ob : run.takeNewHits()) {
            emit(EventType.DELIVERY_BUMP, "obstacle", ob.getType().name(), "condition", round2(run.getCondition()));
            react(Emote.SHOCKED, 1.0);
        }
        if (!run.isFinished()) return;
        hub.clearRun();
        Order o = orders.get(run.getOrderId());
        Customer c = o == null ? null : findCustomer(o.getCustomerId());
        boolean stillWanted = o != null && o.getStatus() == OrderStatus.OUT_FOR_DELIVERY && c != null && !c.getAction().isGone();
        if (run.isSuccess() && stillWanted) {
            finishDelivery(o, c, run.getQuality(), false);
            react(Emote.LAUGH, 1.5);
        } else {
            if (o != null && o.isOpen()) o.cancel();
            if (c != null && !c.getAction().isGone()) { c.leaveAngry(); onCustomerAngry(c); }
            report.recordDelivery(false);
            emit(EventType.DELIVERY_FAILED, "orderId", run.getOrderId(), "reason", run.isSuccess() ? "customer gave up" : "food spilled");
            react(Emote.UH_OH, 2.0);
        }
        emit(EventType.SCENE_CHANGED, "from", SceneId.DELIVERY_HUB.name(), "to", SceneId.DELIVERY_HUB.name(), "environment", hub.getEnvironmentModel());
    }

    private void finishDelivery(Order o, Customer c, double tripQuality, boolean byRider) {
        o.markDelivered();
        c.receiveFood(o.averageQuality() * tripQuality);
        report.recordDelivery(true);
        emit(EventType.DELIVERY_FINISHED, "orderId", o.getId(), "quality", round2(tripQuality), "byRider", byRider);
    }

    // =====================================================================================
    // Progression: XP, goals, achievements
    // =====================================================================================

    private void gainXp(int amount, String reason) {
        report.recordXp(amount);
        List<Integer> ups = progress.addXp(amount);
        emit(EventType.XP_GAINED, "amount", amount, "reason", reason, "xp", progress.getXp(), "level", progress.getLevel());
        for (int lvl : ups) {
            refreshMenu();
            emit(EventType.LEVEL_UP, "level", lvl, "unlocks", String.join("|", Unlocks.textFor(lvl)));
            react(Emote.LAUGH, 3.0);
        }
        if (!ups.isEmpty()) checkAchievements();
    }

    private void checkGoals() {
        for (DailyGoal g : progress.checkGoals(report)) {
            wallet.earn(g.getRewardMoney());
            report.recordBonus(g.getRewardMoney());
            emit(EventType.GOAL_COMPLETED, "title", g.title(), "money", g.getRewardMoney(), "xp", g.getRewardXp(), "done", progress.goalsDone());
            emitMoney();
            gainXp(g.getRewardXp(), "Daily goal");
        }
    }

    private void checkAchievements() {
        progress.setTables(shop.getTables().size());
        for (Achievement a : progress.checkAchievements()) {
            wallet.earn(a.getReward());
            report.recordBonus(a.getReward());
            emit(EventType.ACHIEVEMENT_UNLOCKED, "id", a.getId(), "title", a.getTitle(), "description", a.getDescription(), "reward", a.getReward());
            emitMoney();
        }
    }

    // =====================================================================================
    // Upgrades and staff (allowed any time after the menu: during the day, paused or between days)
    // =====================================================================================

    public CommandResult buyUpgrade(String id) {
        if (state == GameState.MENU) return reject("Start the game first");
        Upgrade u = upgrades.get(id);
        if (u == null) return reject("Unknown upgrade " + id);
        if (progress.getLevel() < u.getUnlockLevel()) return reject(u.getName() + " unlocks at level " + u.getUnlockLevel());
        double cost = upgrades.nextCost(id);
        if (cost < 0) return reject(u.getName() + " is already maxed");
        if (!wallet.spend(cost)) return reject("Not enough money (needs P" + (int) cost + ")");
        upgrades.raise(id);
        report.recordSpent(cost);
        if (UpgradeShop.TABLES.equals(id)) shop.addTable();
        emit(EventType.UPGRADE_BOUGHT, "id", id, "name", u.getName(), "level", upgrades.level(id), "effect", u.effect(upgrades.level(id)));
        emitMoney();
        react(Emote.LAUGH, 2.0);
        checkAchievements();
        return CommandResult.ok(u.getName() + " level " + upgrades.level(id));
    }

    public CommandResult hireWorker(String id) {
        if (state == GameState.MENU) return reject("Start the game first");
        Worker w = staff.get(id);
        if (w == null) return reject("Unknown helper " + id);
        if (w.isHired()) return reject(w.getName() + " already works here");
        if (progress.getLevel() < w.getUnlockLevel()) return reject(w.getName() + " can be hired at level " + w.getUnlockLevel());
        if (!wallet.spend(w.getHireCost())) return reject("Not enough money (needs P" + (int) w.getHireCost() + ")");
        w.hire();
        report.recordSpent(w.getHireCost());
        progress.hired();
        emit(EventType.WORKER_HIRED, "id", id, "name", w.getName(), "role", w.getRole().name());
        emitMoney();
        react(Emote.LAUGH, 2.0);
        checkAchievements();
        return CommandResult.ok(w.getName() + " joined the team!");
    }

    public CommandResult trainWorker(String id) {
        if (state == GameState.MENU) return reject("Start the game first");
        Worker w = staff.get(id);
        if (w == null || !w.isHired()) return reject("Hire them first");
        if (w.getLevel() >= Worker.MAX_LEVEL) return reject(w.getName() + " is already a pro");
        double cost = w.trainCost();
        if (!wallet.spend(cost)) return reject("Not enough money (needs P" + (int) cost + ")");
        w.train();
        report.recordSpent(cost);
        emit(EventType.WORKER_TRAINED, "id", id, "name", w.getName(), "level", w.getLevel());
        emitMoney();
        return CommandResult.ok(w.getName() + " is now level " + w.getLevel());
    }

    public CommandResult setWorkerResting(String id, boolean resting) {
        Worker w = staff.get(id);
        if (w == null || !w.isHired()) return reject("Hire them first");
        w.setResting(resting);
        return CommandResult.ok(w.getName() + (resting ? " is resting" : " is back to work"));
    }

    // ---- StaffContext: what helpers may do (no room restrictions; they walk around themselves) ----
    @Override public Customer customerWaitingToOrder() {
        if (state != GameState.PLAYING) return null;
        return shop.firstAvailableTable() == null ? null : firstInLine();
    }
    @Override public boolean staffTakeOrder(Customer c) { return c.getAction() == CustomerAction.IN_LINE && doTakeOrder(c).isOk(); }
    @Override public Order readyDineInOrder() {
        for (Order o : orders.values()) {
            if (o.getStatus() != OrderStatus.READY || o.isDelivery()) continue;
            Customer c = findCustomer(o.getCustomerId());
            if (c != null && c.getAction() == CustomerAction.WAITING_FOR_FOOD) return o;
        }
        return null;
    }
    @Override public boolean staffServe(Order o) { return doServe(o).isOk(); }
    @Override public Object[] lineForAssistant() {
        CookingSession mine = kitchen().getSession();
        for (Order o : orders.values()) {
            if (!o.isOpen() || o.getStatus() == OrderStatus.READY) continue;
            int l = o.nextLineToCook();
            if (l < 0) continue;
            if (mine != null && mine.getOrderId().equals(o.getId()) && mine.getLineIndex() == l) continue;
            return new Object[]{o, l};
        }
        return null;
    }
    @Override public boolean assistantStart(Order o, int line) {
        OrderLine ol = o.getLines().get(line);
        List<String> needed = new ArrayList<>(ol.getDish().getIngredientIds());
        needed.addAll(ol.getExtras());
        if (!inventory.consume(needed)) { for (String id : inventory.missingFor(needed)) postRestock(id); return false; }
        o.startCooking(line);
        return true;
    }
    @Override public void assistantFinish(Order o, int line, double quality) {
        CookedDish d = new CookedDish(o.getLines().get(line).getDish(), Math.min(1, quality + perks().qualityBonus()), false);
        report.recordCooked();
        emit(EventType.COOKING_FINISHED, "orderId", o.getId(), "dish", d.getDish().getId(), "quality", round2(d.getQuality()),
             "stars", d.getCookStars(), "burnt", false, "byStaff", true);
        if (o.getStatus() == OrderStatus.CANCELLED) return;
        completeLine(o, line, d);
    }
    @Override public Order readyDeliveryOrder() {
        DeliveryMiniGame mine = delivery().getRun();
        for (Order o : orders.values())
            if (o.isDelivery() && o.getStatus() == OrderStatus.READY && (mine == null || !mine.getOrderId().equals(o.getId()))) return o;
        return null;
    }
    @Override public boolean riderDepart(Order o) {
        if (o.getStatus() != OrderStatus.READY) return false;
        o.markOutForDelivery();
        emit(EventType.DELIVERY_STARTED, "orderId", o.getId(), "byRider", true);
        return true;
    }
    @Override public void riderDeliver(Order o, double quality) {
        Customer c = findCustomer(o.getCustomerId());
        if (o.getStatus() == OrderStatus.OUT_FOR_DELIVERY && c != null && !c.getAction().isGone()) finishDelivery(o, c, quality, true);
        else if (o.isOpen()) o.cancel();
    }
    @Override public void staffEvent(Worker w, String what) {
        emit(EventType.STAFF_ACTION, "id", w.getId(), "name", w.getName(), "what", what);
    }

    // =====================================================================================
    // Saving (progress persists between sessions; a loaded game starts at the beginning of the next day)
    // =====================================================================================

    public String exportSave() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("version", 1);
        m.put("day", state == GameState.DAY_OVER ? day + 1 : Math.max(1, day));
        m.put("money", wallet.getBalance());
        m.put("reputation", reputation);
        m.put("progress", progress.save());
        m.put("upgrades", new LinkedHashMap<>(upgrades.levels()));
        Map<String, Object> st = new LinkedHashMap<>();
        for (Worker w : staff.all()) {
            Map<String, Object> wm = new LinkedHashMap<>();
            wm.put("level", w.getLevel()); wm.put("resting", w.isResting());
            st.put(w.getId(), wm);
        }
        m.put("staff", st);
        m.put("inventory", new LinkedHashMap<>(inventory.view()));
        return Json.stringify(m);
    }

    /** Load a save. Only on the title screen (state MENU); the next Start continues from the saved day. */
    @SuppressWarnings("unchecked")
    public CommandResult importSave(String json) {
        if (state != GameState.MENU) return reject("Load a save from the title screen");
        Map<String, Object> m;
        try { m = Json.parseObject(json); } catch (RuntimeException e) { return reject("Save file is damaged"); }
        day = m.get("day") instanceof Number ? ((Number) m.get("day")).intValue() : 1;
        double money = m.get("money") instanceof Number ? ((Number) m.get("money")).doubleValue() : config.startingMoney;
        wallet.spend(wallet.getBalance());
        wallet.earn(money);
        if (m.get("reputation") instanceof Number) reputation = ((Number) m.get("reputation")).doubleValue();
        if (m.get("progress") instanceof Map) progress.load((Map<String, Object>) m.get("progress"));
        if (m.get("upgrades") instanceof Map)
            for (Map.Entry<String, Object> e : ((Map<String, Object>) m.get("upgrades")).entrySet())
                if (e.getValue() instanceof Number) upgrades.setLevel(e.getKey(), ((Number) e.getValue()).intValue());
        if (m.get("staff") instanceof Map)
            for (Map.Entry<String, Object> e : ((Map<String, Object>) m.get("staff")).entrySet()) {
                Worker w = staff.get(e.getKey());
                if (w == null || !(e.getValue() instanceof Map)) continue;
                Map<String, Object> wm = (Map<String, Object>) e.getValue();
                if (wm.get("level") instanceof Number) w.setLevel(((Number) wm.get("level")).intValue());
                w.setResting(Boolean.TRUE.equals(wm.get("resting")));
            }
        if (m.get("inventory") instanceof Map)
            for (Map.Entry<String, Object> e : ((Map<String, Object>) m.get("inventory")).entrySet())
                if (e.getValue() instanceof Number && catalog.contains(e.getKey())) {
                    int want = ((Number) e.getValue()).intValue(), have = inventory.get(e.getKey());
                    if (want > have) inventory.add(e.getKey(), want - have);
                    else if (want < have) for (int i = 0; i < have - want; i++) inventory.consume(java.util.Collections.singletonList(e.getKey()));
                }
        while (shop.getTables().size() < config.tableCount + upgrades.level(UpgradeShop.TABLES)) shop.addTable();
        refreshMenu();
        emit(EventType.GAME_LOADED, "day", day, "level", progress.getLevel(), "money", wallet.getBalance());
        return CommandResult.ok("Welcome back! Day " + day);
    }

    // =====================================================================================
    // GameContext + helpers
    // =====================================================================================

    @Override public double time() { return time; }
    @Override public Random random() { return rng; }
    @Override public Shop shop() { return shop; }
    @Override public Inventory inventory() { return inventory; }
    @Override public Wallet wallet() { return wallet; }
    @Override public void emit(EventType type, Object... kv) { events.emit(type, kv); }
    @Override public void react(Emote emote, double seconds) { if (kaloy.react(emote, seconds)) emitEmote(); }

    private void emitEmote() {
        Emote e = kaloy.getEmote();
        emit(EventType.EMOTE_CHANGED, "emote", e.name(), "atlasCol", e.getAtlasCol(), "atlasRow", e.getAtlasRow());
    }
    private void emitMoney() { emit(EventType.MONEY_CHANGED, "money", wallet.getBalance()); }

    private CommandResult reject(String why) {
        emit(EventType.COMMAND_REJECTED, "reason", why);
        return CommandResult.fail(why);
    }

    private String nextOrderId() { return "o" + (++orderCounter); }
    private static double round2(double v) { return Math.round(v * 100) / 100.0; }

    private KitchenScene kitchen() { return (KitchenScene) scenes.get(SceneId.KITCHEN); }
    private MainDinerScene diner() { return (MainDinerScene) scenes.get(SceneId.MAIN_DINER); }
    private DeliveryHubScene delivery() { return (DeliveryHubScene) scenes.get(SceneId.DELIVERY_HUB); }

    public Customer findCustomer(String id) {
        for (Customer c : customers) if (c.getId().equals(id)) return c;
        return null;
    }

    public void subscribe(GameEventListener l) { events.subscribe(l); }
    public CookingFeedback cookingFeedback() { return cookingFeedback; }
    public CookingSession.Listener cookingListener() { return cookingListener; }

    // read-only views for renderers and the bridge
    public GameState getState() { return state; }
    public GameConfig getConfig() { return config; }
    public Scene getCurrentScene() { return currentScene; }
    public Scene getScene(SceneId id) { return scenes.get(id); }
    public int getDay() { return day; }
    public double getTime() { return time; }
    public boolean isClosed() { return closed; }
    public double getReputation() { return reputation; }
    public Menu getMenu() { return menu; }
    public Menu getFullMenu() { return fullMenu; }
    public IngredientCatalog getCatalog() { return catalog; }
    public Market getMarket() { return market; }
    public TaskBoard getTaskBoard() { return taskBoard; }
    public MangKaloy getKaloy() { return kaloy; }
    public DayReport getReport() { return report; }
    public List<Customer> getCustomers() { return Collections.unmodifiableList(customers); }
    public List<Order> getOrders() { return Collections.unmodifiableList(new ArrayList<>(orders.values())); }
    public Order getOrder(String id) { return orders.get(id); }
    public CookingSession getCookingSession() { return kitchen().getSession(); }
    public DeliveryMiniGame getDeliveryRun() { return delivery().getRun(); }
    public Task getActiveChore() { return diner().getActiveTask(); }
    public Progression getProgress() { return progress; }
    public UpgradeShop getUpgrades() { return upgrades; }
    public Staff getStaff() { return staff; }
    public KitchenPerks getPerks() { return perks(); }
}
