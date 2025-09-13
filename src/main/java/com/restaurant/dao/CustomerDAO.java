package com.restaurant.dao;

import com.restaurant.config.DatabaseConnection;
import com.restaurant.model.Customer;
import com.restaurant.service.interfaces.UserServices;
import com.restaurant.util.LoggerUtil;

import java.sql.*;
import java.util.logging.Logger;

/**
 * this dao is only about customers table, it help to create or update customer records
 */
public class CustomerDAO {
    private static final Logger logger = LoggerUtil.grabLogger();

    public boolean assignTableToCustomer(int customerId, int tableId) {
        String sql = "UPDATE customers SET table_id = ? WHERE customer_id = ?";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, tableId);
            stmt.setInt(2, customerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("problem while linking table to customer " + customerId + ": " + e.getMessage());
            return false;
        }
    }

    public boolean clearTableForCustomer(int customerId) {
        String sql = "UPDATE customers SET table_id = NULL, is_checked_in = FALSE WHERE customer_id = ?";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("problem while clearing table for customer " + customerId + ": " + e.getMessage());
            return false;
        }
    }

    public boolean clearCustomerByTableId(int tableId) {
        String sql = "UPDATE customers SET table_id = NULL, is_checked_in = FALSE WHERE table_id = ?";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, tableId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("problem while clearing customer by table " + tableId + ": " + e.getMessage());
            return false;
        }
    }

    public Customer getCustomerByUserId(int userId) {
        String sql = "SELECT * FROM customers WHERE user_id = ?";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Customer customer = new Customer(rs.getInt("customer_id"),
                        rs.getInt("user_id"),
                        rs.getString("name"));
                customer.setTableId((Integer) rs.getObject("table_id"));
                customer.setCheckedIn(rs.getBoolean("is_checked_in"));
                return customer;
            }
        } catch (SQLException e) {
            logger.warning("cannot fetch customer for userId " + userId + ": " + e.getMessage());
        }
        return null;
    }

    public boolean updateCheckInStatus(int customerId, boolean checkedIn) {
        String sql = "UPDATE customers SET is_checked_in = ? WHERE customer_id = ?";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBoolean(1, checkedIn);
            stmt.setInt(2, customerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.warning("cannot update checkin for customer " + customerId + ": " + e.getMessage());
            return false;
        }
    }
}
