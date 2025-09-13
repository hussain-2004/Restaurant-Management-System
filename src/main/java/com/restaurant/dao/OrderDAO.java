package com.restaurant.dao;

import com.restaurant.config.DatabaseConnection;
import com.restaurant.model.Order;
import com.restaurant.service.interfaces.IOrderService;
import com.restaurant.util.LoggerUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * this dao is for orders, one order belongs to one customer, one table and a waiter
 * we can create new order, get orders, and change status.
 */
public class OrderDAO implements IOrderService {
    private static final Logger logger = LoggerUtil.grabLogger();

    @Override
    public int createOrder(int customerId, int tableId, int waiterId) {
        String sql = "INSERT INTO orders (customer_id, table_id, waiter_id, status) VALUES (?, ?, ?, 'PENDING') RETURNING order_id";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.setInt(2, tableId);
            stmt.setInt(3, waiterId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int orderId = rs.getInt("order_id");
                logger.info("new order " + orderId + " created for customer " + customerId);
                return orderId;
            }
        } catch (SQLException e) {
            logger.severe("error creating order for customer " + customerId + ": " + e.getMessage());
        }
        return -1;
    }

    @Override
    public List<Order> getOrdersByCustomer(int customerId) {
        List<Order> result = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE customer_id = ? ORDER BY order_time DESC";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(new Order(rs.getInt("order_id"),
                        rs.getInt("customer_id"),
                        rs.getInt("table_id"),
                        rs.getInt("waiter_id"),
                        rs.getString("status"),
                        rs.getString("order_time")));
            }
        } catch (SQLException e) {
            logger.warning("error fetching orders for customer " + customerId + ": " + e.getMessage());
        }
        return result;
    }

    @Override
    public boolean updateOrderStatus(int orderId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.warning("error updating status of order " + orderId + ": " + e.getMessage());
            return false;
        }
    }

    public List<Order> getOrdersByStatus(String status) {
        List<Order> result = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE status = ? ORDER BY order_time";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(new Order(rs.getInt("order_id"),
                        rs.getInt("customer_id"),
                        rs.getInt("table_id"),
                        rs.getInt("waiter_id"),
                        rs.getString("status"),
                        rs.getString("order_time")));
            }
        } catch (SQLException e) {
            logger.warning("error fetching orders by status " + status + ": " + e.getMessage());
        }
        return result;
    }
}
