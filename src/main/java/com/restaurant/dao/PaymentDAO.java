package com.restaurant.dao;

import com.restaurant.config.DatabaseConnection;
import com.restaurant.util.LoggerUtil;

import java.sql.*;
import java.util.logging.Logger;

/**
 * Records and manages customer payment transactions against bills.
 */
public class PaymentDAO {
    private static final Logger logger = LoggerUtil.grabLogger();

    public boolean recordPayment(int billId, String method, double amount) {
        String insertQuery = "INSERT INTO payments (bill_id, payment_method, amount) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(insertQuery)) {

            statement.setInt(1, billId);
            statement.setString(2, method);
            statement.setDouble(3, amount);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            logger.warning("error occured recording payment for bill " + billId + ": " + exception.getMessage());
            return false;
        }
    }
}