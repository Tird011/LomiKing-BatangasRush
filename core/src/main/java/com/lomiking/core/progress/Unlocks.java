package com.lomiking.core.progress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** What each player level opens up. Start tiny (Lomi Batangas, 2 tables) and grow. */
public final class Unlocks {
    public static final int MAX_LEVEL = 15;
    public static final int DELIVERY_LEVEL = 4;
    private static final Map<String, Integer> DISH_LEVEL = new LinkedHashMap<>();
    private static final Map<Integer, List<String>> TEXT = new LinkedHashMap<>();
    static {
        DISH_LEVEL.put("lomi_batangas", 1);
        DISH_LEVEL.put("lumpiang_shanghai", 2);
        DISH_LEVEL.put("tapsilog", 4);
        DISH_LEVEL.put("chicken_chami", 6);
        DISH_LEVEL.put("lomi_overload", 8);
        TEXT.put(2, Arrays.asList("New dish: Lumpiang Shanghai", "Upgrade: Extra Table"));
        TEXT.put(3, Arrays.asList("Hire: Ate Joy (server)", "Upgrade: Sharp Itak"));
        TEXT.put(4, Arrays.asList("New dish: Tapsilog", "Phone orders & deliveries", "Upgrade: Turbo Kalan"));
        TEXT.put(5, Arrays.asList("Hire: Kuya Dodong (cleaner)", "Upgrade: Pinoy Decor"));
        TEXT.put(6, Arrays.asList("New dish: Chicken Chami", "Upgrade: Suki Supplier"));
        TEXT.put(7, Arrays.asList("Hire: Mang Tonyo (rider)", "Upgrade: Bagong Motor"));
        TEXT.put(8, Arrays.asList("New dish: Lomi Overload", "Upgrade: Neon Sign"));
        TEXT.put(9, Arrays.asList("Hire: Inday (assistant cook)"));
        TEXT.put(10, Arrays.asList("Upgrade: Recipe ni Lola"));
    }
    private Unlocks() { }

    public static int xpToNext(int level) { return 100 + 90 * (level - 1); }

    public static boolean dishUnlocked(String dishId, int level) { return level >= DISH_LEVEL.getOrDefault(dishId, 99); }
    public static int dishLevel(String dishId) { return DISH_LEVEL.getOrDefault(dishId, 99); }

    public static List<String> unlockedDishes(int level) {
        List<String> out = new ArrayList<>();
        for (Map.Entry<String, Integer> e : DISH_LEVEL.entrySet()) if (level >= e.getValue()) out.add(e.getKey());
        return out;
    }

    public static List<String> textFor(int level) {
        List<String> t = TEXT.get(level);
        return t == null ? Collections.singletonList("Bigger daily goals and rewards") : t;
    }
}
