package com.lomiking.core.economy;

/** Running totals for the current day; shown on the end-of-day screen. */
public class DayReport {
    private final int day;
    private int customersServed, customersAngry, deliveriesDone, deliveriesFailed, tasksDone, tasksExpired, dishesCooked;
    private double revenue, tips, spent, ratingSum;
    private int ratingCount;

    public DayReport(int day) { this.day = day; }

    public void recordSale(double price, double tip, int stars) { revenue += price; tips += tip; ratingSum += stars; ratingCount++; }
    public void recordServed() { customersServed++; }
    public void recordAngry() { customersAngry++; }
    public void recordDelivery(boolean ok) { if (ok) deliveriesDone++; else deliveriesFailed++; }
    public void recordTask(boolean done) { if (done) tasksDone++; else tasksExpired++; }
    public void recordCooked() { dishesCooked++; }
    public void recordSpent(double amount) { spent += amount; }

    public int getDay() { return day; }
    public int getCustomersServed() { return customersServed; }
    public int getCustomersAngry() { return customersAngry; }
    public int getDeliveriesDone() { return deliveriesDone; }
    public int getDeliveriesFailed() { return deliveriesFailed; }
    public int getTasksDone() { return tasksDone; }
    public int getTasksExpired() { return tasksExpired; }
    public int getDishesCooked() { return dishesCooked; }
    public double getRevenue() { return revenue; }
    public double getTips() { return tips; }
    public double getSpent() { return spent; }
    public double getProfit() { return revenue + tips - spent; }
    public double getAverageRating() { return ratingCount == 0 ? 0 : ratingSum / ratingCount; }
}
