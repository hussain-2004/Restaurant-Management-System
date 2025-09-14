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
        String insertBillQuery = "INSERT INTO bills (order_id, total_amount, is_paid) VALUES (?, ?, FALSE) RETURNING bill_id";
        try (Connection databaseConnection = DatabaseConnection.fetchConnection();
             PreparedStatement insertBillStatement = databaseConnection.prepareStatement(insertBillQuery)) {

            insertBillStatement.setInt(1, orderId);
            insertBillStatement.setDouble(2, totalAmount);
            ResultSet billIdResultSet = insertBillStatement.executeQuery();

            if (billIdResultSet.next()) {
                int generatedBillId = billIdResultSet.getInt("bill_id");
                logger.info("bill " + generatedBillId + " created for order " + orderId);
                return generatedBillId;
            }
        } catch (SQLException sqlException) {
            logger.severe("cannot generate bill for order " + orderId + ": " + sqlException.getMessage());
        }
        return -1;
    }

    public boolean markBillAsPaid(int billId) {
        String updateBillPaymentStatusQuery = "UPDATE bills SET is_paid = TRUE WHERE bill_id = ?";
        try (Connection databaseConnection = DatabaseConnection.fetchConnection();
             PreparedStatement updateBillPaymentStatement = databaseConnection.prepareStatement(updateBillPaymentStatusQuery)) {

            updateBillPaymentStatement.setInt(1, billId);
            return updateBillPaymentStatement.executeUpdate() > 0;
        } catch (SQLException sqlException) {
            logger.warning("error marking bill " + billId + " as paid: " + sqlException.getMessage());
            return false;
        }
    }

    public double getBillTotal(int billId) {
        String selectBillTotalQuery = "SELECT total_amount FROM bills WHERE bill_id = ?";
        try (Connection databaseConnection = DatabaseConnection.fetchConnection();
             PreparedStatement selectBillTotalStatement = databaseConnection.prepareStatement(selectBillTotalQuery)) {

            selectBillTotalStatement.setInt(1, billId);
            ResultSet billTotalResultSet = selectBillTotalStatement.executeQuery();

            if (billTotalResultSet.next()) {
                return billTotalResultSet.getDouble("total_amount");
            }
        } catch (SQLException sqlException) {
            logger.warning("error fetching bill total for bill " + billId + ": " + sqlException.getMessage());
        }
        return 0.0;
    }

    public int generateCombinedBill(int customerId, int tableId) {
        String selectOrderItemsWithPricesQuery = "SELECT oi.quantity, m.price " +
                "FROM orders o " +
                "JOIN order_items oi ON o.order_id = oi.order_id " +
                "JOIN menu m ON oi.menu_id = m.menu_id " +
                "WHERE o.customer_id = ? AND o.table_id = ?";

        double combinedBillTotalAmount = 0.0;
        try (Connection databaseConnection = DatabaseConnection.fetchConnection();
             PreparedStatement selectOrderItemsStatement = databaseConnection.prepareStatement(selectOrderItemsWithPricesQuery)) {

            selectOrderItemsStatement.setInt(1, customerId);
            selectOrderItemsStatement.setInt(2, tableId);
            ResultSet orderItemsWithPricesResultSet = selectOrderItemsStatement.executeQuery();

            while (orderItemsWithPricesResultSet.next()) {
                int itemQuantity = orderItemsWithPricesResultSet.getInt("quantity");
                double itemPrice = orderItemsWithPricesResultSet.getDouble("price");
                combinedBillTotalAmount += itemQuantity * itemPrice;
            }

            if (combinedBillTotalAmount > 0) {
                String insertCombinedBillQuery = "INSERT INTO bills (order_id, total_amount, is_paid) VALUES (?, ?, FALSE)";
                int latestOrderIdForCustomer = getLatestOrderIdForCustomer(customerId);

                try (PreparedStatement insertCombinedBillStatement = databaseConnection.prepareStatement(insertCombinedBillQuery, Statement.RETURN_GENERATED_KEYS)) {
                    insertCombinedBillStatement.setInt(1, latestOrderIdForCustomer);
                    insertCombinedBillStatement.setDouble(2, combinedBillTotalAmount);
                    insertCombinedBillStatement.executeUpdate();

                    ResultSet generatedBillIdKeys = insertCombinedBillStatement.getGeneratedKeys();
                    if (generatedBillIdKeys.next()) {
                        return generatedBillIdKeys.getInt(1); // bill_id
                    }
                }
            }
        } catch (SQLException sqlException) {
            LoggerUtil.grabLogger().severe("Error generating combined bill: " + sqlException.getMessage());
        }
        return -1;
    }

    private int getLatestOrderIdForCustomer(int customerId) {
        String selectLatestOrderQuery = "SELECT order_id FROM orders WHERE customer_id = ? ORDER BY order_id DESC LIMIT 1";
        try (Connection databaseConnection = DatabaseConnection.fetchConnection();
             PreparedStatement selectLatestOrderStatement = databaseConnection.prepareStatement(selectLatestOrderQuery)) {

            selectLatestOrderStatement.setInt(1, customerId);
            ResultSet latestOrderResultSet = selectLatestOrderStatement.executeQuery();

            if (latestOrderResultSet.next()) {
                return latestOrderResultSet.getInt("order_id");
            }
        } catch (Exception generalException) {
            LoggerUtil.grabLogger().severe("Error fetching latest order: " + generalException.getMessage());
        }
        return -1;
    }
}