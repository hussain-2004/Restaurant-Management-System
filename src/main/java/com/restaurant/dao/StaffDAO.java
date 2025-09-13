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
 * staff dao fetches staff user details and map to their role classes.
 */
public class StaffDAO {
    private static final Logger logger = LoggerUtil.grabLogger();

    public AbstractStaff getStaffByUserId(int userId) {
        String sql = "SELECT * FROM staff WHERE user_id = ?";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int staffId = rs.getInt("staff_id");
                String name = rs.getString("name");
                String role = rs.getString("role");

                return switch (role.toUpperCase()) {
                    case "WAITER" -> new Waiter(staffId, userId, name);
                    case "CHEF" -> new Chef(staffId, userId, name);
                    case "MANAGER" -> new Manager(staffId, userId, name);
                    case "ADMIN" -> new Admin(staffId, userId, name);
                    default -> null;
                };
            }
        } catch (SQLException e) {
            logger.warning("cannot fetch staff for user " + userId + ": " + e.getMessage());
        }
        return null;
    }
}
