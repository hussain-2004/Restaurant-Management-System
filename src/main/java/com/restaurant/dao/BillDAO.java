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
}
