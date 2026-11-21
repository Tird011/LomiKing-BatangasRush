package com.lomiking.core.food;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Every ingredient in Mang Kaloy's kitchen, looked up by id. */
public final class IngredientCatalog {
    // ids used by recipes (constants avoid typos)
    public static final String NOODLES = "noodles", PORK_BROTH = "pork_broth", CORNSTARCH = "cornstarch";
    public static final String EGG = "egg", QUAIL_EGG = "quail_egg";
    public static final String PORK = "pork", LIVER = "liver", CHICKEN = "chicken", TAPA = "tapa", GROUND_PORK = "ground_pork";
    public static final String ONION = "onion", GARLIC = "garlic", GREEN_ONION = "green_onion", CABBAGE = "cabbage", CARROT = "carrot";
    public static final String SALT = "salt", PEPPER = "paminta", SUGAR = "sugar", CHILI_FLAKES = "chili_flakes";
    public static final String SOY_SAUCE = "soy_sauce", FISH_SAUCE = "patis", OYSTER_SAUCE = "oyster_sauce", VINEGAR = "vinegar";
    public static final String CHICHARON = "chicharon", KIKIAM = "kikiam", FISH_BALL = "fish_ball", SQUID_BALL = "squid_ball", FRIED_GARLIC = "fried_garlic";
    public static final String CALAMANSI = "calamansi", SILI = "sili", ATSARA = "atsara";
    public static final String RICE = "rice", OIL = "oil", LUMPIA_WRAPPER = "lumpia_wrapper";

    private final Map<String, Ingredient> byId = new LinkedHashMap<>();

    public IngredientCatalog() {
        add(new Noodle(NOODLES, "Lomi Noodles (Miki)", 12));
        add(new Broth(PORK_BROTH, "Pork-Bone Broth", 8));
        add(new Thickener(CORNSTARCH, "Cornstarch", 2));
        add(new EggIngredient(EGG, "Egg", 9));
        add(new Topping(QUAIL_EGG, "Quail Eggs", 8));
        add(new Protein(PORK, "Pork Strips", 18, PrepMethod.SLICE));
        add(new Protein(LIVER, "Pork Liver", 15, PrepMethod.SLICE));
        add(new Protein(CHICKEN, "Chicken", 20, PrepMethod.CHOP));
        add(new Protein(TAPA, "Beef Tapa", 35, PrepMethod.FRY));
        add(new Protein(GROUND_PORK, "Ground Pork", 15, PrepMethod.NONE));
        add(new Vegetable(ONION, "Onion", 3));
        add(new Vegetable(GARLIC, "Garlic", 2));
        add(new Vegetable(GREEN_ONION, "Green Onion", 2));
        add(new Vegetable(CABBAGE, "Cabbage", 4));
        add(new Vegetable(CARROT, "Carrot", 3));
        add(new Spice(SALT, "Salt", 1));
        add(new Spice(PEPPER, "Paminta (Black Pepper)", 1));
        add(new Spice(SUGAR, "Sugar", 1));
        add(new Spice(CHILI_FLAKES, "Chili Flakes", 2));
        add(new Sauce(SOY_SAUCE, "Toyo (Soy Sauce)", 2));
        add(new Sauce(FISH_SAUCE, "Patis (Fish Sauce)", 2));
        add(new Sauce(OYSTER_SAUCE, "Oyster Sauce", 3));
        add(new Sauce(VINEGAR, "Sukang Sawsawan (Vinegar)", 1));
        add(new Topping(CHICHARON, "Chicharon", 7));
        add(new Topping(KIKIAM, "Kikiam", 6));
        add(new Topping(FISH_BALL, "Fish Balls", 5));
        add(new Topping(SQUID_BALL, "Squid Balls", 6));
        add(new Topping(FRIED_GARLIC, "Fried Garlic Bits", 2));
        add(new Condiment(CALAMANSI, "Calamansi", 3));
        add(new Condiment(SILI, "Siling Labuyo", 2));
        add(new Condiment(ATSARA, "Atsara", 4));
        add(new Grain(RICE, "Rice", 8));
        add(new PantryItem(OIL, "Cooking Oil", 3));
        add(new PantryItem(LUMPIA_WRAPPER, "Lumpia Wrapper", 5));
    }

    private void add(Ingredient i) { byId.put(i.getId(), i); }

    public Ingredient get(String id) {
        Ingredient i = byId.get(id);
        if (i == null) throw new IllegalArgumentException("Unknown ingredient: " + id);
        return i;
    }
    public boolean contains(String id) { return byId.containsKey(id); }
    public Collection<Ingredient> all() { return Collections.unmodifiableCollection(byId.values()); }
}
