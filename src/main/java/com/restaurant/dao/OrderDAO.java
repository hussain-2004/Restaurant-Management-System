package com.restaurant.dao;

import com.restaurant.config.DatabaseConnection;
import com.restaurant.model.Order;
import com.restaurant.service.interfaces.OrderServiceInterface;
import com.restaurant.util.LoggerUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Manages order operations including creation, retrieval, and status updates for restaurant orders.
 */
public class OrderDAO implements OrderServiceInterface {
    private static final Logger logger = LoggerUtil.grabLogger();

    @Override
    public int createOrder(int customerId, int tableId, int waiterId) {
        String insertQuery = "INSERT INTO orders (customer_id, table_id, waiter_id, status) VALUES (?, ?, ?, 'PENDING') RETURNING order_id";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(insertQuery)) {

            statement.setInt(1, customerId);
            statement.setInt(2, tableId);
            statement.setInt(3, waiterId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int orderId = resultSet.getInt("order_id");
                logger.info("new order " + orderId + " created for customer " + customerId);
                return orderId;
            }
        } catch (SQLException exception) {
            logger.severe("error creating order for customer " + customerId + ": " + exception.getMessage());
        }
        return -1;
    }

    @Override
    public List<Order> getOrdersByCustomer(int customerId) {
        List<Order> orders = new ArrayList<>();
        String selectQuery = "SELECT * FROM orders WHERE customer_id = ? ORDER BY order_time DESC";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setInt(1, customerId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                orders.add(new Order(resultSet.getInt("order_id"),
                        resultSet.getInt("customer_id"),
                        resultSet.getInt("table_id"),
                        resultSet.getInt("waiter_id"),
                        resultSet.getString("status"),
                        resultSet.getString("order_time")));
            }
        } catch (SQLException exception) {
            logger.warning("error fetching orders for customer " + customerId + ": " + exception.getMessage());
        }
        return orders;
    }

    @Override
    public boolean updateOrderStatus(int orderId, String status) {
        String updateQuery = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {

            statement.setString(1, status);
            statement.setInt(2, orderId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            logger.warning("error updating status of order " + orderId + ": " + exception.getMessage());
            return false;
        }
    }

    public List<Order> getOrdersByStatus(String status) {
        List<Order> orders = new ArrayList<>();
        String selectQuery = "SELECT * FROM orders WHERE status = ? ORDER BY order_time";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setString(1, status);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                orders.add(new Order(resultSet.getInt("order_id"),
                        resultSet.getInt("customer_id"),
                        resultSet.getInt("table_id"),
                        resultSet.getInt("waiter_id"),
                        resultSet.getString("status"),
                        resultSet.getString("order_time")));
            }
        } catch (SQLException exception) {
            logger.warning("error fetching orders by status " + status + ": " + exception.getMessage());
        }
        return orders;
    }
}