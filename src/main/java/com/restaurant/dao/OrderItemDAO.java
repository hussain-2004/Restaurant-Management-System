package com.restaurant.dao;

import com.restaurant.config.DatabaseConnection;
import com.restaurant.model.OrderItem;
import com.restaurant.util.LoggerUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Manages individual order items within orders, including status tracking and menu details.
 */
public class OrderItemDAO {
    private static final Logger logger = LoggerUtil.grabLogger();

    public boolean addItemToOrder(int orderId, int menuId, int quantity) {
        String insertQuery = "INSERT INTO order_items (order_id, menu_id, quantity, status) VALUES (?, ?, ?, 'PENDING')";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(insertQuery)) {

            statement.setInt(1, orderId);
            statement.setInt(2, menuId);
            statement.setInt(3, quantity);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            logger.warning("failed to add item to order " + orderId + ": " + exception.getMessage());
            return false;
        }
    }

    public List<OrderItem> getItemsByOrder(int orderId) {
        List<OrderItem> orderItems = new ArrayList<>();
        String selectQuery = "SELECT oi.item_id, oi.order_id, oi.menu_id, oi.quantity, oi.status, m.item_name " +
                "FROM order_items oi " +
                "JOIN menu m ON oi.menu_id = m.menu_id " +
                "WHERE oi.order_id = ?";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setInt(1, orderId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                OrderItem orderItem = new OrderItem(
                        resultSet.getInt("item_id"),
                        resultSet.getInt("order_id"),
                        resultSet.getInt("menu_id"),
                        resultSet.getInt("quantity"),
                        resultSet.getString("status")
                );
                orderItem.setItemName(resultSet.getString("item_name"));
                orderItems.add(orderItem);
            }
        } catch (SQLException exception) {
            logger.warning("cannot fetch items for order " + orderId + ": " + exception.getMessage());
        }
        return orderItems;
    }

    public boolean updateItemStatus(int itemId, String status) {
        String updateQuery = "UPDATE order_items SET status = ? WHERE item_id = ?";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {

            statement.setString(1, status);
            statement.setInt(2, itemId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            logger.warning("cannot update status for item " + itemId + ": " + exception.getMessage());
            return false;
        }
    }
}