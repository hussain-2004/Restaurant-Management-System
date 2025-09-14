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
        String staffSelectionQuery = "SELECT * FROM staff WHERE user_id = ?";
        try (Connection databaseConnectionForStaffOperations = DatabaseConnection.fetchConnection();
             PreparedStatement staffQueryPreparedStatement = databaseConnectionForStaffOperations.prepareStatement(staffSelectionQuery)) {

            staffQueryPreparedStatement.setInt(1, userId);
            ResultSet staffDataResultSet = staffQueryPreparedStatement.executeQuery();

            if (staffDataResultSet.next()) {
                int staffIdentifier = staffDataResultSet.getInt("staff_id");
                String staffMemberName = staffDataResultSet.getString("name");
                String staffRoleType = staffDataResultSet.getString("role");

                return switch (staffRoleType.toUpperCase()) {
                    case "WAITER" -> new Waiter(staffIdentifier, userId, staffMemberName);
                    case "CHEF" -> new Chef(staffIdentifier, userId, staffMemberName);
                    case "MANAGER" -> new Manager(staffIdentifier, userId, staffMemberName);
                    case "ADMIN" -> new Admin(staffIdentifier, userId, staffMemberName);
                    default -> null;
                };
            }
        } catch (SQLException staffRetrievalException) {
            logger.warning("cannot fetch staff details for user " + userId + ": " + staffRetrievalException.getMessage());
        }
        return null;
    }
}