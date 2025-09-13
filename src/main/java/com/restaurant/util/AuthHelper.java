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
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND role = 'CUSTOMER'";
        try (Connection conn = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username.trim());
            stmt.setString(2, password.trim());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new AbstractUser(rs.getInt("user_id"), rs.getString("username")) {};
            }
        } catch (Exception e) {
            logger.severe("Error validating customer login: " + e.getMessage());
            e.printStackTrace(System.err); // print full stack trace for debug
        }
        return null;
    }

    public static AbstractUser validateStaff(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND role IN ('WAITER','CHEF','MANAGER','ADMIN')";
        try (Connection conn = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username.trim());
            stmt.setString(2, password.trim());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new AbstractUser(rs.getInt("user_id"), rs.getString("username")) {};
            }
        } catch (Exception e) {
            logger.severe("Error validating staff login: " + e.getMessage());
            e.printStackTrace(System.err); // print full stack trace for debug
        }
        return null;
    }
}
