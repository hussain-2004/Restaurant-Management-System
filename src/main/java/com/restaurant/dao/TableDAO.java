package com.restaurant.dao;

import com.restaurant.config.DatabaseConnection;
import com.restaurant.model.Table;
import com.restaurant.service.interfaces.TableServiceInterface;
import com.restaurant.util.LoggerUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Manages restaurant table operations including booking, availability checks, and capacity management.
 */
public class TableDAO implements TableServiceInterface {
    private static final Logger logger = LoggerUtil.grabLogger();

    @Override
    public Table getAvailableTable(int requiredSeats) {
        String selectQuery = "SELECT * FROM tables " +
                "WHERE is_booked = FALSE AND capacity >= ? " +
                "ORDER BY capacity ASC LIMIT 1";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setInt(1, requiredSeats);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Table(
                            resultSet.getInt("table_id"),
                            resultSet.getInt("capacity"),
                            resultSet.getBoolean("is_booked"),
                            resultSet.getString("booking_time")
                    );
                }
            }
        } catch (SQLException exception) {
            logger.warning("error occured finding table for " + requiredSeats + " seats: " + exception.getMessage());
        }
        return null;
    }

    @Override
    public boolean assignTable(int tableId) {
        String updateQuery = "UPDATE tables SET is_booked = TRUE, booking_time = NOW() WHERE table_id = ?";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {

            statement.setInt(1, tableId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            logger.warning("error occured assigning table " + tableId + ": " + exception.getMessage());
            return false;
        }
    }

    @Override
    public boolean freeTable(int tableId) {
        String updateQuery = "UPDATE tables SET is_booked = FALSE, booking_time = NULL WHERE table_id = ?";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {

            statement.setInt(1, tableId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            logger.warning("error freeing table " + tableId + ": " + exception.getMessage());
            return false;
        }
    }

    @Override
    public List<Table> getAllTables() {
        List<Table> tables = new ArrayList<>();
        String selectQuery = "SELECT * FROM tables ORDER BY table_id";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                tables.add(new Table(resultSet.getInt("table_id"),
                        resultSet.getInt("capacity"),
                        resultSet.getBoolean("is_booked"),
                        resultSet.getString("booking_time")));
            }
        } catch (SQLException exception) {
            logger.warning("error reading all tables: " + exception.getMessage());
        }
        return tables;
    }

    @Override
    public List<Table> getVacantTables() {
        List<Table> tables = new ArrayList<>();
        String selectQuery = "SELECT * FROM tables WHERE is_booked = FALSE ORDER BY table_id";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                tables.add(new Table(resultSet.getInt("table_id"),
                        resultSet.getInt("capacity"),
                        resultSet.getBoolean("is_booked"),
                        resultSet.getString("booking_time")));
            }
        } catch (SQLException exception) {
            logger.warning("error reading vacant tables: " + exception.getMessage());
        }
        return tables;
    }
}