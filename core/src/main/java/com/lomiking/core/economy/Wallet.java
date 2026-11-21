package com.lomiking.core.economy;

/** Mang Kaloy's cash box. */
public class Wallet {
    private double balance;
    public Wallet(double startingBalance) { this.balance = startingBalance; }
    public double getBalance() { return balance; }
    public void earn(double amount) {
        if (amount < 0) throw new IllegalArgumentException("negative amount");
        balance += amount;
    }
    /** @return false (and spends nothing) if there isn't enough cash */
    public boolean spend(double amount) {
        if (amount < 0) throw new IllegalArgumentException("negative amount");
        if (amount > balance) return false;
        balance -= amount;
        return true;
    }
    public boolean canAfford(double amount) { return amount <= balance; }
}
