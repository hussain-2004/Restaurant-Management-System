package com.restaurant.dao;

import com.restaurant.config.DatabaseConnection;
import com.restaurant.model.Table;
import com.restaurant.service.interfaces.ITableService;
import com.restaurant.util.LoggerUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * dao for tables, the actual furniture where people sit.
 */
public class TableDAO implements ITableService {
    private static final Logger logger = LoggerUtil.grabLogger();

    @Override
    public Table getAvailableTable() {
        String sql = "SELECT * FROM tables WHERE is_booked = FALSE LIMIT 1";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return new Table(rs.getInt("table_id"),
                        rs.getInt("capacity"),
                        rs.getBoolean("is_booked"),
                        rs.getString("booking_time"));
            }
        } catch (SQLException e) {
            logger.warning("error finding available table: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean assignTable(int tableId) {
        String sql = "UPDATE tables SET is_booked = TRUE, booking_time = NOW() WHERE table_id = ?";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, tableId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.warning("error assigning table " + tableId + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean freeTable(int tableId) {
        String sql = "UPDATE tables SET is_booked = FALSE, booking_time = NULL WHERE table_id = ?";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, tableId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.warning("error freeing table " + tableId + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Table> getAllTables() {
        List<Table> result = new ArrayList<>();
        String sql = "SELECT * FROM tables ORDER BY table_id";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(new Table(rs.getInt("table_id"),
                        rs.getInt("capacity"),
                        rs.getBoolean("is_booked"),
                        rs.getString("booking_time")));
            }
        } catch (SQLException e) {
            logger.warning("error reading all tables: " + e.getMessage());
        }
        return result;
    }

    @Override
    public List<Table> getVacantTables() {
        List<Table> result = new ArrayList<>();
        String sql = "SELECT * FROM tables WHERE is_booked = FALSE ORDER BY table_id";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(new Table(rs.getInt("table_id"),
                        rs.getInt("capacity"),
                        rs.getBoolean("is_booked"),
                        rs.getString("booking_time")));
            }
        } catch (SQLException e) {
            logger.warning("error reading vacant tables: " + e.getMessage());
        }
        return result;
    }
}
