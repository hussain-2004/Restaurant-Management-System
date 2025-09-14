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
 * dao for tables, the actual furniture where people sit.
 */
public class TableDAO implements TableServiceInterface {
    private static final Logger logger = LoggerUtil.grabLogger();

    @Override
    public Table getAvailableTable(int requiredSeats) {
        String selectBestFitTableQuery = "SELECT * FROM tables " +
                "WHERE is_booked = FALSE AND capacity >= ? " +
                "ORDER BY capacity ASC LIMIT 1";
        try (Connection databaseConnectionForTableSearch = DatabaseConnection.fetchConnection();
             PreparedStatement findAvailableTablePreparedStatement = databaseConnectionForTableSearch.prepareStatement(selectBestFitTableQuery)) {

            findAvailableTablePreparedStatement.setInt(1, requiredSeats);
            try (ResultSet availableTableResultSet = findAvailableTablePreparedStatement.executeQuery()) {
                if (availableTableResultSet.next()) {
                    return new Table(
                            availableTableResultSet.getInt("table_id"),
                            availableTableResultSet.getInt("capacity"),
                            availableTableResultSet.getBoolean("is_booked"),
                            availableTableResultSet.getString("booking_time")
                    );
                }
            }
        } catch (SQLException tableSearchException) {
            logger.warning("error occured finding table for " + requiredSeats + " seats: " + tableSearchException.getMessage());
        }
        return null;
    }

    @Override
    public boolean assignTable(int tableId) {
        String updateTableBookingStatusQuery = "UPDATE tables SET is_booked = TRUE, booking_time = NOW() WHERE table_id = ?";
        try (Connection databaseConnectionForTableAssignment = DatabaseConnection.fetchConnection();
             PreparedStatement assignTablePreparedStatement = databaseConnectionForTableAssignment.prepareStatement(updateTableBookingStatusQuery)) {

            assignTablePreparedStatement.setInt(1, tableId);
            return assignTablePreparedStatement.executeUpdate() > 0;
        } catch (SQLException tableAssignmentException) {
            logger.warning("error occured assigning table " + tableId + ": " + tableAssignmentException.getMessage());
            return false;
        }
    }

    @Override
    public boolean freeTable(int tableId) {
        String clearTableBookingQuery = "UPDATE tables SET is_booked = FALSE, booking_time = NULL WHERE table_id = ?";
        try (Connection databaseConnectionForTableRelease = DatabaseConnection.fetchConnection();
             PreparedStatement freeTablePreparedStatement = databaseConnectionForTableRelease.prepareStatement(clearTableBookingQuery)) {

            freeTablePreparedStatement.setInt(1, tableId);
            return freeTablePreparedStatement.executeUpdate() > 0;
        } catch (SQLException tableReleaseException) {
            logger.warning("error freeing table " + tableId + ": " + tableReleaseException.getMessage());
            return false;
        }
    }

    @Override
    public List<Table> getAllTables() {
        List<Table> completeTablesList = new ArrayList<>();
        String selectAllTablesQuery = "SELECT * FROM tables ORDER BY table_id";
        try (Connection databaseConnectionForAllTables = DatabaseConnection.fetchConnection();
             PreparedStatement getAllTablesPreparedStatement = databaseConnectionForAllTables.prepareStatement(selectAllTablesQuery);
             ResultSet allTablesResultSet = getAllTablesPreparedStatement.executeQuery()) {

            while (allTablesResultSet.next()) {
                completeTablesList.add(new Table(allTablesResultSet.getInt("table_id"),
                        allTablesResultSet.getInt("capacity"),
                        allTablesResultSet.getBoolean("is_booked"),
                        allTablesResultSet.getString("booking_time")));
            }
        } catch (SQLException allTablesRetrievalException) {
            logger.warning("error reading all tables: " + allTablesRetrievalException.getMessage());
        }
        return completeTablesList;
    }

    @Override
    public List<Table> getVacantTables() {
        List<Table> emptyTablesList = new ArrayList<>();
        String selectVacantTablesQuery = "SELECT * FROM tables WHERE is_booked = FALSE ORDER BY table_id";
        try (Connection databaseConnectionForVacantTables = DatabaseConnection.fetchConnection();
             PreparedStatement getVacantTablesPreparedStatement = databaseConnectionForVacantTables.prepareStatement(selectVacantTablesQuery);
             ResultSet vacantTablesResultSet = getVacantTablesPreparedStatement.executeQuery()) {

            while (vacantTablesResultSet.next()) {
                emptyTablesList.add(new Table(vacantTablesResultSet.getInt("table_id"),
                        vacantTablesResultSet.getInt("capacity"),
                        vacantTablesResultSet.getBoolean("is_booked"),
                        vacantTablesResultSet.getString("booking_time")));
            }
        } catch (SQLException vacantTablesException) {
            logger.warning("error reading vacant tables: " + vacantTablesException.getMessage());
        }
        return emptyTablesList;
    }
}