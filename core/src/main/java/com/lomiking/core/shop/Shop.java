package com.lomiking.core.shop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The lomihan itself: tables, the dirty-dish pile and the floor. */
public class Shop {
    private final List<Table> tables = new ArrayList<>();
    private int dirtyDishes;
    private double floorDirt;

    public Shop(int tableCount) { for (int i = 1; i <= tableCount; i++) tables.add(new Table(i)); }

    public List<Table> getTables() { return Collections.unmodifiableList(tables); }
    public Table getTable(int id) {
        for (Table t : tables) if (t.getId() == id) return t;
        throw new IllegalArgumentException("No table " + id);
    }
    public Table firstAvailableTable() {
        for (Table t : tables) if (t.isAvailable()) return t;
        return null;
    }
    public Table seat(String customerId) {
        Table t = firstAvailableTable();
        if (t != null) t.seat(customerId);
        return t;
    }
    public void vacate(int tableId, double mess, int dishes) {
        getTable(tableId).vacate(mess);
        dirtyDishes += dishes;
        floorDirt = Math.min(1, floorDirt + 0.08);
    }

    public int getDirtyDishes() { return dirtyDishes; }
    public void washDishes(int count) { dirtyDishes = Math.max(0, dirtyDishes - count); }
    public double getFloorDirt() { return floorDirt; }
    public void sweep(double amount) { floorDirt = Math.max(0, floorDirt - amount); }
    public void addFloorDirt(double amount) { floorDirt = Math.min(1, floorDirt + amount); }
}
