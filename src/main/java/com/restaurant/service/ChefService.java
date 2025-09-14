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

    private final OrderDAO orderDataAccessLayer = new OrderDAO();
    private final OrderItemDAO orderItemDataAccessLayer = new OrderItemDAO();

    public List<Order> getPendingOrders() {
        List<Order> pendingOrdersListForKitchen = orderDataAccessLayer.getOrdersByStatus("PENDING");
        return pendingOrdersListForKitchen;
    }

    public List<OrderItem> getItemsForOrder(int orderId) {
        List<OrderItem> orderItemsListForSpecificOrder = orderItemDataAccessLayer.getItemsByOrder(orderId);
        return orderItemsListForSpecificOrder;
    }

    public boolean markOrderItemCompleted(int itemId) {
        boolean itemCompletionUpdateResult = orderItemDataAccessLayer.updateItemStatus(itemId, "COMPLETED");
        if (itemCompletionUpdateResult) {
            logger.info("order item " + itemId + " marked completed by chef");
        }
        return itemCompletionUpdateResult;
    }

    public boolean markOrderReady(int orderId) {
        boolean orderReadyStatusUpdateResult = orderDataAccessLayer.updateOrderStatus(orderId, "READY");
        if (orderReadyStatusUpdateResult) {
            logger.info("order " + orderId + " marked ready by chef");
        }
        return orderReadyStatusUpdateResult;
    }
}