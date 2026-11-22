# LomiKing: Batangas Rush, architecture

## 1. Layers

```
+--------------------------------------------------------------------------+
|  React Native app (mobile/, TypeScript, Expo)                            |
|                                                                          |
|   UI (React components)            3D (three.js on expo-gl)              |
|   Hud, NotesBoard, RoomNav,        Stage -> Room subclasses:             |
|   Counter/Kitchen/Diner/Hub        Title, Counter, Kitchen, Diner,       |
|   panels, GesturePad, mic          Hub, Road  (+ Character rigs)         |
|          \                         /                                     |
|           +--- GameClient (src/engine) ---+                              |
|                 queues commands, runs the logic loop, holds the snapshot |
+-----------------------------|--------------------------------------------+
                              |  CoreBridge: step(dt, commands) -> {results, events, snapshot}
              +---------------+-----------------+
              |                                 |
   NativeCoreBridge (Android build)    HttpCoreBridge (web, Expo Go)
   TurboModule "LomiCore" (Java)       POST http://<pc>:8787/step
   mobile/modules/lomi-core            devserver/ (Java, JDK HttpServer)
              |                                 |
              +---------------+-----------------+
                              v
+--------------------------------------------------------------------------+
|  core/ (pure Java 11, no Android, no dependencies)                       |
|   GameBridge -> CommandParser -> GameCommand -> GameEngine               |
|   GameEngine owns: Scenes, Customers, Orders, Kitchen, TaskBoard,        |
|   Shop, Inventory, Wallet, Market, MangKaloy                             |
+--------------------------------------------------------------------------+
```

**Java is the single source of truth for game rules.** TypeScript never decides whether an order is ready,
how hot the stove is, or how many stars a customer gives. It draws the snapshot and sends what the player did.
The Android module compiles `core/src/main/java` directly (see `modules/lomi-core/android/build.gradle`),
so there is exactly one copy of the logic.

## 2. One logic tick (every 50 ms)

```
GameClient.loop()
  commands = queue (taps, swipes, mic level, "takeOrder", "goTo"...)
  result   = bridge.step(dt, commands)          // ONE call: run commands, engine.update(dt), build snapshot
  snapshot -> React (max ~10 refreshes/s) and three.js (reads it every frame, ~60 fps, animates in between)
  events   -> toasts, haptics, and room animations (CUSTOMER_PAID, DELIVERY_BUMP, ...)
```

Doing everything in one call per tick keeps the JS <-> Java traffic small. On Android the TurboModule methods are
synchronous (`isBlockingSynchronousMethod`). Over HTTP the same JSON goes through the dev server.

**Platform note:** the Java core runs inside the app on Android only. iOS would need the dev-server mode or a port.

## 2b. Rendering
- Models are made in Blender by script (`blender/`), exported as `.glb`, loaded with three's `GLTFLoader`.
- `toon.ts` swaps the Blender materials for `MeshToonMaterial` (3-band cel shading) and adds an inverted-hull outline.
- Mang Kaloy's face is a separate UV-mapped mesh; the drawn faces live in one atlas and the Java `Emote`
  (atlasCol/atlasRow) picks the cell, so the same expressions show in 3D and in the HUD.
- Characters are rigid parts animated procedurally (walk bob, arm swings, chop/stir/fan poses).
- Each room has a landscape and a portrait camera (the bottom panel covers ~40% of a phone screen).

## 3. OOP map (what to point at for grading)

| Principle | Where |
|---|---|
| **Abstraction** | `Ingredient`, `Dish`, `CookingStep`, `Customer`, `Task`, `Scene` are abstract. `GameContext` and `GameCommand` are interfaces. |
| **Inheritance** | `LomiOverload extends LomiBatangas` (only changes toppings and price). `BoilStep`/`FryStep extend TimedStep extends CookingStep`. 5 customer types extend `Customer`. |
| **Polymorphism** | The kitchen runs any `List<CookingStep>` a dish returns. Each `Customer` subclass overrides `chooseDishes`, `tipRate`, `qualityWeight`, `basePatience`. Each `Scene` handles the same `PlayerInput` differently. Each `Ingredient` subclass returns its own `PrepMethod`. |
| **Encapsulation** | State changes only go through methods that check the rules: `Order.markServed()` throws unless READY, `Inventory.consume()` is all-or-nothing, `Wallet.spend()` refuses overspending, `Table.seat()` is package-private to `Shop`. |
| **Design patterns** | State (`Scene`, `CustomerAction`, `OrderStatus`, `TaskStatus`, `GameState`), Command (`GameCommand`), Observer (`EventBus`), Factory (`CustomerFactory`, `Menu.standard()`), Template Method (`CookingStep.update` -> `onUpdate`, `Task.update` -> `isGoalMet`), Facade (`GameBridge`). |

**v2 packages:** `upgrade/` (abstract `Upgrade` with 8 subclasses, cost curve in the base class: Template Method), `staff/` (abstract `Worker`, 4 subclasses each overriding `work()`, they act through the narrow `StaffContext` interface the engine implements), `progress/` (`Progression`, `Unlocks`, abstract `DailyGoal` with 6 subclasses, `Achievement`), `cooking/KitchenPerks` (upgrade levels the cooking steps read: Strategy-like parameter object).

## 4. State machines

**Game:** `MENU -> PLAYING <-> PAUSED`, `PLAYING -> DAY_OVER -> PLAYING (next day)`

**Rooms (docs/images/game_flow.png):** the counter is the hub.
`MENU -> COUNTER`; `COUNTER <-> KITCHEN`, `COUNTER <-> MAIN_DINER`, `COUNTER <-> DELIVERY_HUB`.
You can't leave the delivery hub mid-ride.

**Customer:** `ARRIVING -> IN_LINE -> WAITING_FOR_FOOD -> EATING -> PAYING -> LEFT`.
Patience running out in line or while waiting leads to `LEFT_ANGRY`.
Delivery customers skip the line.

**Order:** `PENDING -> COOKING -> READY -> SERVED`, or for deliveries `READY -> OUT_FOR_DELIVERY -> DELIVERED`.
Any open order becomes `CANCELLED` if the customer leaves.

**Task (hanging note):** `POSTED -> ACTIVE -> DONE`, or `EXPIRED`.

## 5. The rooms and their notes

| Sector / room | Notes (tasks) | Mini-games |
|---|---|---|
| Counter | (hub) | take order, serve |
| Kitchen | `CookOrderTask` per order | Chop (swipe), Add ingredients (drag), Fan the fire (mic/swipe), Boil/Fry (timed tap), Stir (circles), Season (tap + done), Plate (drag extras + done) |
| Main diner | `CleanTableTask`, `WashDishesTask`, `SweepFloorTask` | wipe (swipe), scrub (tap), sweep (circles) |
| Delivery hub | `DeliveryTask`, `RestockTask` | motorbike lane-dodging ride, market buying |

Chores create gameplay pressure. A customer can't be seated at a dirty table, and dishes can't be cooked without stock.

## 6. Command reference (JSON into `GameBridge.dispatch`)

```json
{"cmd":"start"}                              {"cmd":"pause"} {"cmd":"resume"} {"cmd":"nextDay"}
{"cmd":"goTo","scene":"KITCHEN"}             COUNTER | KITCHEN | MAIN_DINER | DELIVERY_HUB
{"cmd":"takeOrder"}                          or {"cmd":"takeOrder","customerId":"c3"}
{"cmd":"cook","orderId":"o1"}                orderId optional (oldest first)
{"cmd":"input","type":"SWIPE","value":0.6,"dx":1,"dy":0}
{"cmd":"input","type":"TAP"}                 TAP | SWIPE | CIRCLE | HOLD | MIC_LEVEL | DRAG_DROP | CONFIRM
{"cmd":"input","type":"DRAG_DROP","itemId":"noodles"}
{"cmd":"input","type":"MIC_LEVEL","value":0.8}
{"cmd":"serve","orderId":"o1"}
{"cmd":"startTask","taskId":"t4"}
{"cmd":"buy","ingredientId":"egg","qty":10}
{"cmd":"deliver","orderId":"o2"}
{"cmd":"buyUpgrade","id":"tables"}           tables | knife | stove | decor | supplier | scooter | sign | recipe
{"cmd":"hire","id":"server"}                 server | cleaner | rider | cook
{"cmd":"train","id":"server"}                {"cmd":"rest","id":"cook","resting":true}
```
Saves: `GameBridge.exportSave()` returns JSON; `importSave(json)` restores it (title screen only).
Every call returns `{"ok":true|false,"message":"..."}`. Rejections also arrive as a `COMMAND_REJECTED` event, so the UI can show a toast.

## 7. Where to change things
| I want to... | Edit |
|---|---|
| Balance numbers (day length, spawn rate, patience) | `core/.../engine/GameConfig.java`, customer classes |
| Add a dish | new `Dish` subclass + `Menu.standard()`, food prop in `blender/environments.py`, name mapping in `three/rooms/Room.ts` |
| Add a cooking mini-game | new `CookingStep` subclass + a case in `ui/panels/KitchenPanel.tsx` |
| Add a customer type | new `Customer` subclass + `CustomerFactory` + model in `blender/characters.py` |
| Change a room's look | `blender/environments.py`, then the room class in `mobile/src/three/rooms/` |
