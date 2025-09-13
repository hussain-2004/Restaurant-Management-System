package com.restaurant.service;

import com.restaurant.dao.OrderDAO;
import com.restaurant.dao.OrderItemDAO;
import com.restaurant.model.Order;
import com.restaurant.model.OrderItem;
import com.restaurant.util.LoggerUtil;

import java.util.List;
import java.util.logging.Logger;

/**
 * waiter service is for waiter persons,
 * they see orders, bring food when ready and mark as served.
 */
public class WaiterService {
    private static final Logger logger = LoggerUtil.grabLogger();

    private final OrderDAO orderDao = new OrderDAO();
    private final OrderItemDAO orderItemDao = new OrderItemDAO();

    public List<Order> getOrdersForWaiter(int waiterId) {
        return orderDao.getOrdersByStatus("PENDING").stream()
                .filter(order -> order.getWaiterId() == waiterId)
                .toList();
    }

    public List<OrderItem> getItemsByOrder(int orderId) {
        return orderItemDao.getItemsByOrder(orderId);
    }

    public boolean markOrderAsServed(int orderId) {
        boolean updated = orderDao.updateOrderStatus(orderId, "SERVED");
        if (updated) {
            logger.info("order " + orderId + " marked served by waiter");
        }
        return updated;
    }
}
