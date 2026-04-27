package com.edusmart.dao;

import com.edusmart.model.Order;
import com.edusmart.model.OrderItem;

import java.util.List;

public interface OrderDao {
    /**
     * Creates one row in {@code orders} + all rows in {@code order_items} in a single transaction.
     * Returns created order id.
     */
    int createOrderWithItems(Order order, List<OrderItem> items);

    void updateStripeSessionId(int orderId, String stripeSessionId);
}

