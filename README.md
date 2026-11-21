# LomiKing: Batangas Rush

A light-hearted 3D cooking and shop-running game for Android. You play as Mang Kaloy, running a Batangas lomihan:
take orders at the counter, cook lomi, tapsilog and chicken chami through touch and mic mini-games,
keep the diner clean, restock at the palengke and ride out deliveries.

| Layer | Tech | Folder |
|---|---|---|
| Game logic (all rules) | **Java 11**, OOP, no dependencies | `core/` |
| App + UI | **React Native** (Expo SDK 58, TypeScript) | `mobile/` |
| 3D graphics | **three.js** on expo-gl | `mobile/src/three/` |
| Java ↔ React Native | Android TurboModule (Java) | `mobile/modules/lomi-core/` |
| Dev server (Java logic over HTTP) | JDK `HttpServer` | `devserver/` |
| 3D models | **Blender** Python scripts → `.glb` | `blender/` |

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for how the pieces connect.

## Play it
| Script | What it does | Needs |
|---|---|---|
| `run-web.bat` | Java logic on your PC + the game in your browser | JDK 17+, Node 20+ |
| `run-phone.bat` | Same, but on your phone through **Expo Go** (same Wi-Fi) | JDK 17+, Node 20+, Expo Go app |
| `run-android.bat` | Builds the real Android app with the Java logic **inside** it | + Android Studio (SDK), USB debugging or emulator |
| `run.bat` | Runs the 18 Java logic tests + a bot playing one day | JDK 11+ |

Expo Go can't load custom native code, so in Expo Go the app talks to `run-devserver.bat` on your PC instead.
The installed Android build (`run-android.bat`) runs the same Java classes on the phone itself.

## Rebuild the art
- **Models:** `blender --background --python blender/characters.py` and `blender --background --python blender/environments.py`
  (writes `mobile/assets/models/*.glb`). Change colors, sizes or props in the scripts and re-run.
- **Mang Kaloy's faces:** `python tools/make_face_atlas.py <folder with the face drawings>`, which makes `mobile/assets/textures/face_atlas.png`.
- **Logo:** `python tools/make_logo.py <your sketch.png>`.

## Status: v1
- [x] Java core: rooms, 5 customer types, orders, 8 cooking mini-games, chores, delivery run, palengke, economy, day cycle
- [x] React Native app: title screen, 4 rooms + delivery road in three.js, HUD, hanging notes, mic blowing, day summary
- [x] Blender models: Mang Kaloy (cleaned up), 4 customers, lomi house, counter, kitchen, diner, hub, road, jeepney, tricycle, dog, chicken, food
- [x] Tested end to end in a browser against the real Java logic (dine-in loop + delivery loop)
- [ ] Not yet tested on a physical Android phone (the native module build needs Android Studio on your PC)
- [ ] Sound effects and music
