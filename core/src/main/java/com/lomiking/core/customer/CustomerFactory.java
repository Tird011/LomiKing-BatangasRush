package com.lomiking.core.customer;

import java.util.Random;

/** Creates walk-in and delivery customers with Filipino names (factory pattern). */
public class CustomerFactory {
    private static final String[] FIRST = {"Juan", "Maria", "Jose", "Ana", "Pedro", "Liza", "Rommel", "Joy", "Carlo", "Bea", "Nonoy", "Inday", "Marites", "Dodong", "Neneng"};
    private static final String[] TITA = {"Tita Baby", "Tita Lourdes", "Tita Cora", "Tita Mameng"};
    private static final String[] DRIVER = {"Mang Berto", "Kuya Jun", "Mang Ramon", "Boss Dante"};
    private static final String[] ADDRESS = {"Lipa Poblacion", "Brgy. Marawoy", "Brgy. Sabang", "Batangas City Port", "Tanauan Plaza", "Brgy. Balintawak"};
    private final Random rng;
    private int counter;

    public CustomerFactory(Random rng) { this.rng = rng; }

    public Customer createWalkIn(double reputation) {
        String id = "c" + (++counter);
        double r = rng.nextDouble();
        // better reputation attracts more titas (big tippers)
        double titaChance = 0.1 + 0.05 * Math.max(0, reputation - 3);
        if (r < titaChance) return new TitaCustomer(id, TITA[rng.nextInt(TITA.length)]);
        if (r < titaChance + 0.25) return new StudentCustomer(id, FIRST[rng.nextInt(FIRST.length)]);
        if (r < titaChance + 0.45) return new JeepneyDriver(id, DRIVER[rng.nextInt(DRIVER.length)]);
        return new RegularCustomer(id, FIRST[rng.nextInt(FIRST.length)]);
    }

    public DeliveryCustomer createDelivery() {
        String id = "c" + (++counter);
        return new DeliveryCustomer(id, FIRST[rng.nextInt(FIRST.length)], ADDRESS[rng.nextInt(ADDRESS.length)]);
    }
}
