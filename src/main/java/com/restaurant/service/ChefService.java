package com.restaurant.service;

import com.restaurant.dao.OrderDAO;
import com.restaurant.dao.OrderItemDAO;
import com.restaurant.model.Order;
import com.restaurant.model.OrderItem;
import com.restaurant.util.LoggerUtil;

import java.util.List;
import java.util.logging.Logger;

/**
 * chef service is for cook persons,
 * they look at pending orders and mark them done after cooking.
 */
public class ChefService {
    private static final Logger logger = LoggerUtil.grabLogger();

    private final OrderDAO orderDao = new OrderDAO();
    private final OrderItemDAO orderItemDao = new OrderItemDAO();

    public List<Order> getPendingOrders() {
        return orderDao.getOrdersByStatus("PENDING");
    }

    public List<OrderItem> getItemsForOrder(int orderId) {
        return orderItemDao.getItemsByOrder(orderId);
    }

    public boolean markOrderItemCompleted(int itemId) {
        boolean ok = orderItemDao.updateItemStatus(itemId, "COMPLETED");
        if (ok) {
            logger.info("order item " + itemId + " marked completed by chef");
        }
        return ok;
    }

    public boolean markOrderReady(int orderId) {
        boolean ok = orderDao.updateOrderStatus(orderId, "READY");
        if (ok) {
            logger.info("order " + orderId + " marked ready by chef");
        }
        return ok;
    }
}
