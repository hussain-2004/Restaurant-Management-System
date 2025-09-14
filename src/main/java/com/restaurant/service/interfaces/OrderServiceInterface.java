package com.restaurant.service.interfaces;

import com.restaurant.model.Order;

import java.util.List;

public interface OrderServiceInterface {
    int createOrder(int customerId, int tableId, int waiterId);

    List<Order> getOrdersByCustomer(int customerId);

    boolean updateOrderStatus(int orderId, String status);
}
