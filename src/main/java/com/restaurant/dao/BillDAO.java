package com.restaurant.dao;

import com.restaurant.config.DatabaseConnection;
import com.restaurant.model.Order;
import com.restaurant.util.LoggerUtil;

import java.sql.*;
import java.util.logging.Logger;

/**
 * Manages bill generation and payment tracking for restaurant orders.
 */
public class BillDAO {
    private static final Logger logger = LoggerUtil.grabLogger();

    public int generateBill(int orderId, double totalAmount) {
        String billQuery = "INSERT INTO bills (order_id, total_amount, is_paid) VALUES (?, ?, FALSE) RETURNING bill_id";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(billQuery)) {

            statement.setInt(1, orderId);
            statement.setDouble(2, totalAmount);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int billId = resultSet.getInt("bill_id");
                logger.info("bill " + billId + " created for order " + orderId);
                return billId;
            }
        } catch (SQLException exception) {
            logger.severe("cannot generate bill for order " + orderId + ": " + exception.getMessage());
        }
        return -1;
    }

    public boolean markBillAsPaid(int billId) {
        String updateQuery = "UPDATE bills SET is_paid = TRUE WHERE bill_id = ?";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {

            statement.setInt(1, billId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            logger.warning("error marking bill " + billId + " as paid: " + exception.getMessage());
            return false;
        }
    }

    public double getBillTotal(int billId) {
        String selectQuery = "SELECT total_amount FROM bills WHERE bill_id = ?";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setInt(1, billId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getDouble("total_amount");
            }
        } catch (SQLException exception) {
            logger.warning("error fetching bill total for bill " + billId + ": " + exception.getMessage());
        }
        return 0.0;
    }

    public int generateCombinedBill(int customerId, int tableId) {
        String itemsQuery = "SELECT oi.quantity, m.price " +
                "FROM orders o " +
                "JOIN order_items oi ON o.order_id = oi.order_id " +
                "JOIN menu m ON oi.menu_id = m.menu_id " +
                "WHERE o.customer_id = ? AND o.table_id = ?";

        double totalAmount = 0.0;
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(itemsQuery)) {

            statement.setInt(1, customerId);
            statement.setInt(2, tableId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int quantity = resultSet.getInt("quantity");
                double price = resultSet.getDouble("price");
                totalAmount += quantity * price;
            }

            if (totalAmount > 0) {
                String insertQuery = "INSERT INTO bills (order_id, total_amount, is_paid) VALUES (?, ?, FALSE)";
                int orderId = getLatestOrderIdForCustomer(customerId);

                try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                    insertStatement.setInt(1, orderId);
                    insertStatement.setDouble(2, totalAmount);
                    insertStatement.executeUpdate();

                    ResultSet keys = insertStatement.getGeneratedKeys();
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        } catch (SQLException exception) {
            LoggerUtil.grabLogger().severe("Error generating combined bill: " + exception.getMessage());
        }
        return -1;
    }

    private int getLatestOrderIdForCustomer(int customerId) {
        String orderQuery = "SELECT order_id FROM orders WHERE customer_id = ? ORDER BY order_id DESC LIMIT 1";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(orderQuery)) {

            statement.setInt(1, customerId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("order_id");
            }
        } catch (Exception exception) {
            LoggerUtil.grabLogger().severe("Error fetching latest order: " + exception.getMessage());
        }
        return -1;
    }
}