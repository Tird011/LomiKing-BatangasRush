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
public class GameEngine implements GameContext {
    private final GameConfig config;
    private final Random rng;
    private final EventBus events;
    private final Menu menu = Menu.standard();
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
        beginDay(1);
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
        emit(EventType.DAY_STARTED, "day", d, "money", wallet.getBalance());
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
        if (!customers.isEmpty()) shop.addFloorDirt(dt * 0.002);
        postChores();
        for (Task t : taskBoard.update(dt, this)) onTaskFinished(t);
        if (closed && customers.isEmpty() && delivery().getRun() == null) endDay();
    }

    private void endDay() {
        state = GameState.DAY_OVER;
        emit(EventType.DAY_ENDED, "day", day, "served", report.getCustomersServed(), "angry", report.getCustomersAngry(),
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
        spawnTimer = (config.spawnMinSeconds + rng.nextDouble() * (config.spawnMaxSeconds - config.spawnMinSeconds)) * repFactor;

        if (rng.nextDouble() < config.deliveryChance) {
            DeliveryCustomer c = customerFactory.createDelivery();
            customers.add(c);
            Order o = c.placeOrder(menu, rng, nextOrderId(), time);
            orders.put(o.getId(), o);
            post(new CookOrderTask(taskBoard.nextId(), o));
            post(new DeliveryTask(taskBoard.nextId(), o, c.getAddress()));
            emit(EventType.CUSTOMER_ARRIVED, "customerId", c.getId(), "type", c.getTypeLabel(), "name", c.getName(), "model", c.getModelId(), "remote", true);
            emit(EventType.ORDER_CREATED, "orderId", o.getId(), "customerId", c.getId(), "summary", o.summary(), "delivery", true);
        } else if (countInLine() < config.maxQueue) {
            Customer c = customerFactory.createWalkIn(reputation);
            customers.add(c);
            emit(EventType.CUSTOMER_ARRIVED, "customerId", c.getId(), "type", c.getTypeLabel(), "name", c.getName(), "model", c.getModelId(), "remote", false);
        }
    }

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
        wallet.earn(p.getTotal());
        report.recordSale(p.getPrice(), p.getTip(), r.getStars());
        reputation = reputation * 0.9 + r.getStars() * 0.1;
        if (c.getTableId() > 0) shop.vacate(c.getTableId(), 0.4 + 0.15 * c.getOrder().getLines().size(), c.getOrder().getLines().size());
        react(r.getStars() >= 5 ? Emote.LAUGH : r.getStars() <= 2 ? Emote.NERVOUS : Emote.HAPPY, 2.0);
        emit(EventType.CUSTOMER_PAID, "customerId", c.getId(), "stars", r.getStars(), "comment", r.getComment(),
             "price", p.getPrice(), "tip", p.getTip());
        emitMoney();
    }

    /** At the counter: take the order of the customer at the front (or a specific one). */
    public CommandResult takeOrder(String customerId) {
        if (state != GameState.PLAYING) return reject("Not playing");
        if (currentScene.getId() != SceneId.COUNTER) return reject("Go to the counter to take orders");
        Customer c = customerId == null ? firstInLine() : findCustomer(customerId);
        if (c == null) return reject("No customer waiting");
        if (c.getAction() != CustomerAction.IN_LINE) return reject(c.getName() + " isn't in line");
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
        CookingSession s = new CookingSession(o.getId(), line, ol.getDish(), ol.getExtras());
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
        if (o == null || o.getStatus() == OrderStatus.CANCELLED) return;   // customer left meanwhile: wasted
        o.finishCooking(s.getLineIndex(), result);
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
        hub.setRun(new DeliveryMiniGame(o.getId(), config.deliveryDistance, o.maxFragility(), rng));
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
            o.markDelivered();
            c.receiveFood(o.averageQuality() * run.getQuality());
            report.recordDelivery(true);
            emit(EventType.DELIVERY_FINISHED, "orderId", o.getId(), "quality", round2(run.getQuality()));
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
}
