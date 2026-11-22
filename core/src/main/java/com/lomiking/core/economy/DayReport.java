package com.lomiking.core.economy;

/** Running totals for the current day; shown on the end-of-day screen. */
public class DayReport {
    private final int day;
    private int customersServed, customersAngry, deliveriesDone, deliveriesFailed, tasksDone, tasksExpired, dishesCooked, fiveStars, perfectDishes, chores;
    private double revenue, tips, spent, ratingSum, wages, bonuses, xpGained;
    private int ratingCount;

    public DayReport(int day) { this.day = day; }

    public void recordSale(double price, double tip, int stars) { revenue += price; tips += tip; ratingSum += stars; ratingCount++; if (stars >= 5) fiveStars++; }
    public void recordPerfectDish() { perfectDishes++; }
    public void recordChore() { chores++; }
    public int getChores() { return chores; }
    public void recordWages(double w) { wages += w; spent += w; }
    public void recordBonus(double b) { bonuses += b; }
    public void recordXp(double xp) { xpGained += xp; }
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
    public double getProfit() { return revenue + tips + bonuses - spent; }
    public int getFiveStars() { return fiveStars; }
    public int getPerfectDishes() { return perfectDishes; }
    public double getWages() { return wages; }
    public double getBonuses() { return bonuses; }
    public double getXpGained() { return xpGained; }
    public double getEarned() { return revenue + tips; }
    public double getAverageRating() { return ratingCount == 0 ? 0 : ratingSum / ratingCount; }
}
