package com.lomiking.core.food;

import java.util.Objects;

/**
 * Base class for everything that goes into a dish. Subclasses decide how the ingredient is prepared
 * (polymorphism) while the shared data (id, name, cost) stays encapsulated here.
 */
public abstract class Ingredient {
    private final String id;
    private final String displayName;
    private final double unitCost;   // pesos per portion, used by the market

    protected Ingredient(String id, String displayName, double unitCost) {
        this.id = Objects.requireNonNull(id);
        this.displayName = Objects.requireNonNull(displayName);
        if (unitCost < 0) throw new IllegalArgumentException("cost must be >= 0");
        this.unitCost = unitCost;
    }

    public final String getId() { return id; }
    public final String getDisplayName() { return displayName; }
    public final double getUnitCost() { return unitCost; }

    public abstract IngredientCategory getCategory();
    public abstract PrepMethod getPrepMethod();

    /** Fresh items spoil faster in the pantry; override where it matters. */
    public boolean isPerishable() { return false; }

    public String describePrep() { return getDisplayName() + ": " + getPrepMethod().getGestureHint(); }

    @Override public boolean equals(Object o) { return o instanceof Ingredient && ((Ingredient) o).id.equals(id); }
    @Override public int hashCode() { return id.hashCode(); }
    @Override public String toString() { return displayName; }
}
