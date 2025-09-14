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
        String sql = "INSERT INTO order_items (order_id, menu_id, quantity, status) VALUES (?, ?, ?, 'PENDING')";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.setInt(2, menuId);
            stmt.setInt(3, quantity);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.warning("failed to add item to order " + orderId + ": " + e.getMessage());
            return false;
        }
    }

    public List<OrderItem> getItemsByOrder(int orderId) {
        List<OrderItem> result = new ArrayList<>();
        String sql = "SELECT oi.item_id, oi.order_id, oi.menu_id, oi.quantity, oi.status, m.item_name " +
                "FROM order_items oi " +
                "JOIN menu m ON oi.menu_id = m.menu_id " +
                "WHERE oi.order_id = ?";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                OrderItem item = new OrderItem(
                        rs.getInt("item_id"),
                        rs.getInt("order_id"),
                        rs.getInt("menu_id"),
                        rs.getInt("quantity"),
                        rs.getString("status")
                );
                item.setItemName(rs.getString("item_name")); // 🔹 add dish name
                result.add(item);
            }
        } catch (SQLException e) {
            logger.warning("cannot fetch items for order " + orderId + ": " + e.getMessage());
        }
        return result;
    }


    public boolean updateItemStatus(int itemId, String status) {
        String sql = "UPDATE order_items SET status = ? WHERE item_id = ?";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, itemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.warning("cannot update status for item " + itemId + ": " + e.getMessage());
            return false;
        }
    }
}
