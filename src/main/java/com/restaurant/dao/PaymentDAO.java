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
        String paymentInsertionQuery = "INSERT INTO payments (bill_id, payment_method, amount) VALUES (?, ?, ?)";
        try (Connection databaseConnectionForPayments = DatabaseConnection.fetchConnection();
             PreparedStatement insertPaymentPreparedStatement = databaseConnectionForPayments.prepareStatement(paymentInsertionQuery)) {

            insertPaymentPreparedStatement.setInt(1, billId);
            insertPaymentPreparedStatement.setString(2, method);
            insertPaymentPreparedStatement.setDouble(3, amount);
            return insertPaymentPreparedStatement.executeUpdate() > 0;
        } catch (SQLException paymentProcessingException) {
            logger.warning("error occured recording payment for bill " + billId + ": " + paymentProcessingException.getMessage());
            return false;
        }
    }
}