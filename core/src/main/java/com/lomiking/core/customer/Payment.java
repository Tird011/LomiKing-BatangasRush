package com.lomiking.core.customer;

public final class Payment {
    private final double price, tip;
    public Payment(double price, double tip) { this.price = price; this.tip = tip; }
    public double getPrice() { return price; }
    public double getTip() { return tip; }
    public double getTotal() { return price + tip; }
}
