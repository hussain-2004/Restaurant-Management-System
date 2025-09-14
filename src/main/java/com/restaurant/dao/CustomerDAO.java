package com.restaurant.dao;

import com.restaurant.config.DatabaseConnection;
import com.restaurant.model.Customer;
import com.restaurant.util.LoggerUtil;

import java.sql.*;
import java.util.logging.Logger;

/**
 * Manages customer table operations including table assignments and check-in status.
 */
public class CustomerDAO {
    private static final Logger logger = LoggerUtil.grabLogger();

    public boolean assignTableToCustomer(int customerId, int tableId) {
        String updateQuery = "UPDATE customers SET table_id = ? WHERE customer_id = ?";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {

            statement.setInt(1, tableId);
            statement.setInt(2, customerId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            logger.severe("problem while linking table to customer " + customerId + ": " + exception.getMessage());
            return false;
        }
    }

    public boolean clearTableForCustomer(int customerId) {
        String updateQuery = "UPDATE customers SET table_id = NULL, is_checked_in = FALSE WHERE customer_id = ?";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {

            statement.setInt(1, customerId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            logger.severe("problem while clearing table for customer " + customerId + ": " + exception.getMessage());
            return false;
        }
    }

    public boolean clearCustomerByTableId(int tableId) {
        String updateQuery = "UPDATE customers SET table_id = NULL, is_checked_in = FALSE WHERE table_id = ?";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {

            statement.setInt(1, tableId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            logger.severe("problem while clearing customer by table " + tableId + ": " + exception.getMessage());
            return false;
        }
    }

    public Customer getCustomerByUserId(int userId) {
        String selectQuery = "SELECT * FROM customers WHERE user_id = ?";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Customer customer = new Customer(resultSet.getInt("customer_id"),
                        resultSet.getInt("user_id"),
                        resultSet.getString("name"));
                customer.setTableId((Integer) resultSet.getObject("table_id"));
                customer.setCheckedIn(resultSet.getBoolean("is_checked_in"));
                return customer;
            }
        } catch (SQLException exception) {
            logger.warning("cannot fetch customer for userId " + userId + ": " + exception.getMessage());
        }
        return null;
    }

    public boolean updateCheckInStatus(int customerId, boolean checkedIn) {
        String updateQuery = "UPDATE customers SET is_checked_in = ? WHERE customer_id = ?";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {

            statement.setBoolean(1, checkedIn);
            statement.setInt(2, customerId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            logger.warning("cannot update checkin for customer " + customerId + ": " + exception.getMessage());
            return false;
        }
    }
}