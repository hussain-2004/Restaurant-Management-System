package com.restaurant.dao;

import com.restaurant.config.DatabaseConnection;
import com.restaurant.model.OrderItem;
import com.restaurant.util.LoggerUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * order item dao, handle single dish in a order, can be updated to cooking or completed.
 */
public class OrderItemDAO {
    private static final Logger logger = LoggerUtil.grabLogger();

    public boolean addItemToOrder(int orderId, int menuId, int quantity) {
        String insertOrderItemQuery = "INSERT INTO order_items (order_id, menu_id, quantity, status) VALUES (?, ?, ?, 'PENDING')";
        try (Connection databaseConnection = DatabaseConnection.fetchConnection();
             PreparedStatement insertOrderItemStatement = databaseConnection.prepareStatement(insertOrderItemQuery)) {

            insertOrderItemStatement.setInt(1, orderId);
            insertOrderItemStatement.setInt(2, menuId);
            insertOrderItemStatement.setInt(3, quantity);
            return insertOrderItemStatement.executeUpdate() > 0;
        } catch (SQLException sqlException) {
            logger.warning("failed to add item to order " + orderId + ": " + sqlException.getMessage());
            return false;
        }
    }

    public List<OrderItem> getItemsByOrder(int orderId) {
        List<OrderItem> orderItemsListWithMenuDetails = new ArrayList<>();
        String selectOrderItemsWithMenuNamesQuery = "SELECT oi.item_id, oi.order_id, oi.menu_id, oi.quantity, oi.status, m.item_name " +
                "FROM order_items oi " +
                "JOIN menu m ON oi.menu_id = m.menu_id " +
                "WHERE oi.order_id = ?";
        try (Connection databaseConnection = DatabaseConnection.fetchConnection();
             PreparedStatement selectOrderItemsWithMenuStatement = databaseConnection.prepareStatement(selectOrderItemsWithMenuNamesQuery)) {

            selectOrderItemsWithMenuStatement.setInt(1, orderId);
            ResultSet orderItemsWithMenuDataResultSet = selectOrderItemsWithMenuStatement.executeQuery();

            while (orderItemsWithMenuDataResultSet.next()) {
                OrderItem orderItemFromDatabase = new OrderItem(
                        orderItemsWithMenuDataResultSet.getInt("item_id"),
                        orderItemsWithMenuDataResultSet.getInt("order_id"),
                        orderItemsWithMenuDataResultSet.getInt("menu_id"),
                        orderItemsWithMenuDataResultSet.getInt("quantity"),
                        orderItemsWithMenuDataResultSet.getString("status")
                );
                orderItemFromDatabase.setItemName(orderItemsWithMenuDataResultSet.getString("item_name")); // 🔹 add dish name
                orderItemsListWithMenuDetails.add(orderItemFromDatabase);
            }
        } catch (SQLException sqlException) {
            logger.warning("cannot fetch items for order " + orderId + ": " + sqlException.getMessage());
        }
        return orderItemsListWithMenuDetails;
    }

    public boolean updateItemStatus(int itemId, String status) {
        String updateOrderItemStatusQuery = "UPDATE order_items SET status = ? WHERE item_id = ?";
        try (Connection databaseConnection = DatabaseConnection.fetchConnection();
             PreparedStatement updateItemStatusStatement = databaseConnection.prepareStatement(updateOrderItemStatusQuery)) {

            updateItemStatusStatement.setString(1, status);
            updateItemStatusStatement.setInt(2, itemId);
            return updateItemStatusStatement.executeUpdate() > 0;
        } catch (SQLException sqlException) {
            logger.warning("cannot update status for item " + itemId + ": " + sqlException.getMessage());
            return false;
        }
    }
}