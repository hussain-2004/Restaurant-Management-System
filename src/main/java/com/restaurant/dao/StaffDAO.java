package com.restaurant.dao;

import com.restaurant.config.DatabaseConnection;
import com.restaurant.model.AbstractStaff;
import com.restaurant.model.Chef;
import com.restaurant.model.Manager;
import com.restaurant.model.Waiter;
import com.restaurant.model.Admin;
import com.restaurant.util.LoggerUtil;

import java.sql.*;
import java.util.logging.Logger;

/**
 * Retrieves staff user details and maps them to their corresponding role classes.
 */
public class StaffDAO {
    private static final Logger logger = LoggerUtil.grabLogger();

    public AbstractStaff getStaffByUserId(int userId) {
        String selectQuery = "SELECT * FROM staff WHERE user_id = ?";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int staffId = resultSet.getInt("staff_id");
                String name = resultSet.getString("name");
                String role = resultSet.getString("role");

                return switch (role.toUpperCase()) {
                    case "WAITER" -> new Waiter(staffId, userId, name);
                    case "CHEF" -> new Chef(staffId, userId, name);
                    case "MANAGER" -> new Manager(staffId, userId, name);
                    case "ADMIN" -> new Admin(staffId, userId, name);
                    default -> null;
                };
            }
        } catch (SQLException exception) {
            logger.warning("cannot fetch staff details for user " + userId + ": " + exception.getMessage());
        }
        return null;
    }
}