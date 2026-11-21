package com.lomiking.core.shop;

/** A diner table. A customer can only sit at a free, clean table. */
public class Table {
    private final int id;
    private String occupantId;
    private double dirt;   // 0 clean .. 1 very messy

    public Table(int id) { this.id = id; }
    public int getId() { return id; }
    public boolean isFree() { return occupantId == null; }
    public boolean isClean() { return dirt <= 0.0001; }
    public boolean isAvailable() { return isFree() && isClean(); }
    public String getOccupantId() { return occupantId; }
    public double getDirt() { return dirt; }

    void seat(String customerId) {
        if (!isAvailable()) throw new IllegalStateException("Table " + id + " is not available");
        occupantId = customerId;
    }
    void vacate(double mess) { occupantId = null; dirt = Math.min(1, dirt + mess); }
    public void wipe(double amount) { dirt = Math.max(0, dirt - amount); }
}
