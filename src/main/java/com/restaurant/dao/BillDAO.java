package com.restaurant.dao;

import com.restaurant.config.DatabaseConnection;
import com.restaurant.model.Order;
import com.restaurant.util.LoggerUtil;

import java.sql.*;
import java.util.logging.Logger;

/**
 * bill dao makes bill for a order and track if paid or not.
 */
public class BillDAO {
    private static final Logger logger = LoggerUtil.grabLogger();

    public int generateBill(int orderId, double totalAmount) {
        String sql = "INSERT INTO bills (order_id, total_amount, is_paid) VALUES (?, ?, FALSE) RETURNING bill_id";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.setDouble(2, totalAmount);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int billId = rs.getInt("bill_id");
                logger.info("bill " + billId + " created for order " + orderId);
                return billId;
            }
        } catch (SQLException e) {
            logger.severe("cannot generate bill for order " + orderId + ": " + e.getMessage());
        }
        return -1;
    }

    public boolean markBillAsPaid(int billId) {
        String sql = "UPDATE bills SET is_paid = TRUE WHERE bill_id = ?";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, billId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.warning("error marking bill " + billId + " as paid: " + e.getMessage());
            return false;
        }
    }

    public double getBillTotal(int billId) {
        String sql = "SELECT total_amount FROM bills WHERE bill_id = ?";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, billId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total_amount");
            }
        } catch (SQLException e) {
            logger.warning("error fetching bill total for bill " + billId + ": " + e.getMessage());
        }
        return 0.0;
    }

    public int generateCombinedBill(int customerId, int tableId) {
        String sql = "SELECT oi.quantity, m.price " +
                "FROM orders o " +
                "JOIN order_items oi ON o.order_id = oi.order_id " +
                "JOIN menu m ON oi.menu_id = m.menu_id " +
                "WHERE o.customer_id = ? AND o.table_id = ?";

        double total = 0.0;
        try (Connection conn = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.setInt(2, tableId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int qty = rs.getInt("quantity");
                double price = rs.getDouble("price");
                total += qty * price;
            }

            if (total > 0) {
                String insertBill = "INSERT INTO bills (order_id, total_amount, is_paid) VALUES (?, ?, FALSE)";
                int latestOrderId = getLatestOrderIdForCustomer(customerId);
                try (PreparedStatement insertStmt = conn.prepareStatement(insertBill, Statement.RETURN_GENERATED_KEYS)) {
                    insertStmt.setInt(1, latestOrderId);
                    insertStmt.setDouble(2, total);
                    insertStmt.executeUpdate();

                    ResultSet keys = insertStmt.getGeneratedKeys();
                    if (keys.next()) {
                        return keys.getInt(1); // bill_id
                    }
                }
            }
        } catch (SQLException e) {
            LoggerUtil.grabLogger().severe("Error generating combined bill: " + e.getMessage());
        }
        return -1;
    }

    private int getLatestOrderIdForCustomer(int customerId) {
        String sql = "SELECT order_id FROM orders WHERE customer_id = ? ORDER BY order_id DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("order_id");
            }
        } catch (Exception e) {
            LoggerUtil.grabLogger().severe("Error fetching latest order: " + e.getMessage());
        }
        return -1;
    }

}
