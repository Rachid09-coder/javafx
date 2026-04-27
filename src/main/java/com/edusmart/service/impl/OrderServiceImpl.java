package com.edusmart.service.impl;

import com.edusmart.dao.OrderDao;
import com.edusmart.model.Order;
import com.edusmart.model.OrderItem;
import com.edusmart.service.OrderService;

import java.util.List;

public class OrderServiceImpl implements OrderService {
    private final OrderDao orderDao;

    public OrderServiceImpl(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    @Override
    public int createOrderWithItems(Order order, List<OrderItem> items) {
        validate(order, items);
        return orderDao.createOrderWithItems(order, items);
    }

    private void validate(Order order, List<OrderItem> items) {
        if (order == null) throw new IllegalArgumentException("Order is required.");
        if (order.getStudentId() <= 0) throw new IllegalArgumentException("Student is required.");
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("Cart is empty.");
        if (order.getTotalAfterDiscount() < 0) throw new IllegalArgumentException("Total is invalid.");
    }
}

