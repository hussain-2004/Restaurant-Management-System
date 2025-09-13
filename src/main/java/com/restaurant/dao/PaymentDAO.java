package com.restaurant.dao;

import com.restaurant.config.DatabaseConnection;
import com.restaurant.util.LoggerUtil;

import java.sql.*;
import java.util.logging.Logger;

/**
 * payment dao record payments of customers against bills.
 */
public class PaymentDAO {
    private static final Logger logger = LoggerUtil.grabLogger();

    public boolean recordPayment(int billId, String method, double amount) {
        String sql = "INSERT INTO payments (bill_id, payment_method, amount) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, billId);
            stmt.setString(2, method);
            stmt.setDouble(3, amount);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.warning("error recording payment for bill " + billId + ": " + e.getMessage());
            return false;
        }
    }
}
