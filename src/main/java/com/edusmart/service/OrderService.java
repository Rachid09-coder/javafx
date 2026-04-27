package com.edusmart.service;

import com.edusmart.model.Order;
import com.edusmart.model.OrderItem;

import java.util.List;

public interface OrderService {
    int createOrderWithItems(Order order, List<OrderItem> items);

    void updateStripeSessionId(int orderId, String stripeSessionId);

    List<Order> getAllOrders();
    List<OrderItem> getAllOrderItems();
}

