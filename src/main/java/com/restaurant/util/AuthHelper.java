package com.restaurant.util;

import com.restaurant.config.DatabaseConnection;
import com.restaurant.model.AbstractUser;

import java.sql.*;
import java.util.logging.Logger;

/**
 * Utility class for validating user login credentials against the users table.
 */
public class AuthHelper {
    private static final Logger logger = LoggerUtil.grabLogger();

    public static AbstractUser validateCustomer(String username, String password) {
        String selectQuery = "SELECT * FROM users WHERE username = ? AND password = ? AND role = 'CUSTOMER'";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setString(1, username.trim());
            statement.setString(2, password.trim());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new AbstractUser(resultSet.getInt("user_id"), resultSet.getString("username")) {};
            }
        } catch (Exception exception) {
            logger.severe("Error validating customer login: " + exception.getMessage());
            exception.printStackTrace(System.err);
        }
        return null;
    }

    public static AbstractUser validateStaff(String username, String password) {
        String selectQuery = "SELECT * FROM users WHERE username = ? AND password = ? AND role IN ('WAITER','CHEF','MANAGER','ADMIN')";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setString(1, username.trim());
            statement.setString(2, password.trim());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new AbstractUser(resultSet.getInt("user_id"), resultSet.getString("username")) {};
            }
        } catch (Exception exception) {
            logger.severe("Error validating staff login: " + exception.getMessage());
            exception.printStackTrace(System.err);
        }
        return null;
    }
}