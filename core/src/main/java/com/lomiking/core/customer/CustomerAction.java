package com.lomiking.core.customer;

/** Where a customer is in their visit (their state machine). */
public enum CustomerAction {
    ARRIVING,          // walking in
    IN_LINE,           // at the counter waiting to order
    WAITING_FOR_FOOD,  // seated, order taken
    EATING,
    PAYING,            // finished eating, pays on the way out
    LEFT,              // gone, happy or not
    LEFT_ANGRY;        // ran out of patience

    public boolean isGone() { return this == LEFT || this == LEFT_ANGRY; }
}
