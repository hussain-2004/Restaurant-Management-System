package com.restaurant.dao;

import com.restaurant.config.DatabaseConnection;
import com.restaurant.model.Customer;
import com.restaurant.util.LoggerUtil;

import java.sql.*;
import java.util.logging.Logger;

/**
 * this dao is only about customers table, it helps to create or update customer records
 */
public class CustomerDAO {
    private static final Logger logger = LoggerUtil.grabLogger();

    public boolean assignTableToCustomer(int customerId, int tableId) {
        String updateCustomerTableAssignmentQuery = "UPDATE customers SET table_id = ? WHERE customer_id = ?";
        try (Connection databaseConnection = DatabaseConnection.fetchConnection();
             PreparedStatement updateTableAssignmentStatement = databaseConnection.prepareStatement(updateCustomerTableAssignmentQuery)) {

            updateTableAssignmentStatement.setInt(1, tableId);
            updateTableAssignmentStatement.setInt(2, customerId);
            return updateTableAssignmentStatement.executeUpdate() > 0;
        } catch (SQLException sqlException) {
            logger.severe("problem while linking table to customer " + customerId + ": " + sqlException.getMessage());
            return false;
        }
    }

    public boolean clearTableForCustomer(int customerId) {
        String clearCustomerTableAndCheckInQuery = "UPDATE customers SET table_id = NULL, is_checked_in = FALSE WHERE customer_id = ?";
        try (Connection databaseConnection = DatabaseConnection.fetchConnection();
             PreparedStatement clearCustomerTableStatement = databaseConnection.prepareStatement(clearCustomerTableAndCheckInQuery)) {

            clearCustomerTableStatement.setInt(1, customerId);
            return clearCustomerTableStatement.executeUpdate() > 0;
        } catch (SQLException sqlException) {
            logger.severe("problem while clearing table for customer " + customerId + ": " + sqlException.getMessage());
            return false;
        }
    }

    public boolean clearCustomerByTableId(int tableId) {
        String clearAllCustomersFromTableQuery = "UPDATE customers SET table_id = NULL, is_checked_in = FALSE WHERE table_id = ?";
        try (Connection databaseConnection = DatabaseConnection.fetchConnection();
             PreparedStatement clearCustomersFromTableStatement = databaseConnection.prepareStatement(clearAllCustomersFromTableQuery)) {

            clearCustomersFromTableStatement.setInt(1, tableId);
            return clearCustomersFromTableStatement.executeUpdate() > 0;
        } catch (SQLException sqlException) {
            logger.severe("problem while clearing customer by table " + tableId + ": " + sqlException.getMessage());
            return false;
        }
    }

    public Customer getCustomerByUserId(int userId) {
        String selectCustomerByUserIdQuery = "SELECT * FROM customers WHERE user_id = ?";
        try (Connection databaseConnection = DatabaseConnection.fetchConnection();
             PreparedStatement selectCustomerByUserIdStatement = databaseConnection.prepareStatement(selectCustomerByUserIdQuery)) {

            selectCustomerByUserIdStatement.setInt(1, userId);
            ResultSet customerDataResultSet = selectCustomerByUserIdStatement.executeQuery();

            if (customerDataResultSet.next()) {
                Customer customerFromDatabase = new Customer(customerDataResultSet.getInt("customer_id"),
                        customerDataResultSet.getInt("user_id"),
                        customerDataResultSet.getString("name"));
                customerFromDatabase.setTableId((Integer) customerDataResultSet.getObject("table_id"));
                customerFromDatabase.setCheckedIn(customerDataResultSet.getBoolean("is_checked_in"));
                return customerFromDatabase;
            }
        } catch (SQLException sqlException) {
            logger.warning("cannot fetch customer for userId " + userId + ": " + sqlException.getMessage());
        }
        return null;
    }

    public boolean updateCheckInStatus(int customerId, boolean checkedIn) {
        String updateCustomerCheckInStatusQuery = "UPDATE customers SET is_checked_in = ? WHERE customer_id = ?";
        try (Connection databaseConnection = DatabaseConnection.fetchConnection();
             PreparedStatement updateCheckInStatusStatement = databaseConnection.prepareStatement(updateCustomerCheckInStatusQuery)) {

            updateCheckInStatusStatement.setBoolean(1, checkedIn);
            updateCheckInStatusStatement.setInt(2, customerId);
            return updateCheckInStatusStatement.executeUpdate() > 0;
        } catch (SQLException sqlException) {
            logger.warning("cannot update checkin for customer " + customerId + ": " + sqlException.getMessage());
            return false;
        }
    }
}