package com.lomiking.core.staff;

import com.lomiking.core.customer.Customer;
import com.lomiking.core.customer.Order;
import com.lomiking.core.shop.Shop;

/** The shop operations staff are allowed to do. GameEngine implements this. */
public interface StaffContext {
    Shop shop();
    /** First walk-in customer waiting to order, or null. */
    Customer customerWaitingToOrder();
    boolean staffTakeOrder(Customer c);
    Order readyDineInOrder();
    boolean staffServe(Order o);
    /** An order line nobody is cooking yet: {order, lineIndex} or null. */
    Object[] lineForAssistant();
    boolean assistantStart(Order o, int line);
    void assistantFinish(Order o, int line, double quality);
    Order readyDeliveryOrder();
    boolean riderDepart(Order o);
    void riderDeliver(Order o, double quality);
    void staffEvent(Worker w, String what);
}
