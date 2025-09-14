package com.restaurant.util;

import com.restaurant.config.DatabaseConnection;
import com.restaurant.model.AbstractUser;

import java.sql.*;
import java.util.logging.Logger;

/**
 * small helper to check login credentials from users table
 * it does not belong to service or dao, so kept separate in util.
 */
public class AuthHelper {
    private static final Logger logger = LoggerUtil.grabLogger();

    public static AbstractUser validateCustomer(String username, String password) {
        String customerValidationQuery = "SELECT * FROM users WHERE username = ? AND password = ? AND role = 'CUSTOMER'";
        try (Connection databaseConnectionForCustomerAuth = DatabaseConnection.fetchConnection();
             PreparedStatement customerValidationPreparedStatement = databaseConnectionForCustomerAuth.prepareStatement(customerValidationQuery)) {

            customerValidationPreparedStatement.setString(1, username.trim());
            customerValidationPreparedStatement.setString(2, password.trim());
            ResultSet customerValidationResultSet = customerValidationPreparedStatement.executeQuery();
            if (customerValidationResultSet.next()) {
                return new AbstractUser(customerValidationResultSet.getInt("user_id"), customerValidationResultSet.getString("username")) {};
            }
        } catch (Exception customerAuthenticationException) {
            logger.severe("Error validating customer login: " + customerAuthenticationException.getMessage());
            customerAuthenticationException.printStackTrace(System.err);
        }
        return null;
    }

    public static AbstractUser validateStaff(String username, String password) {
        String staffValidationQuery = "SELECT * FROM users WHERE username = ? AND password = ? AND role IN ('WAITER','CHEF','MANAGER','ADMIN')";
        try (Connection databaseConnectionForStaffAuth = DatabaseConnection.fetchConnection();
             PreparedStatement staffValidationPreparedStatement = databaseConnectionForStaffAuth.prepareStatement(staffValidationQuery)) {

            staffValidationPreparedStatement.setString(1, username.trim());
            staffValidationPreparedStatement.setString(2, password.trim());
            ResultSet staffValidationResultSet = staffValidationPreparedStatement.executeQuery();
            if (staffValidationResultSet.next()) {
                return new AbstractUser(staffValidationResultSet.getInt("user_id"), staffValidationResultSet.getString("username")) {};
            }
        } catch (Exception staffAuthenticationException) {
            logger.severe("Error validating staff login: " + staffAuthenticationException.getMessage());
            staffAuthenticationException.printStackTrace(System.err);
        }
        return null;
    }
}