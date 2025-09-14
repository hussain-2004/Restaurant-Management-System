package com.restaurant.service;

import com.restaurant.config.DatabaseConnection;
import com.restaurant.dao.*;
import com.restaurant.model.Order;
import com.restaurant.model.OrderItem;
import com.restaurant.model.Table;
import com.restaurant.util.LoggerUtil;
import com.restaurant.util.QueueManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Logger;

/**
 * Handles manager operations including order viewing, billing, payments, and table management.
 */
public class ManagerService {
    private static final Logger logger = LoggerUtil.grabLogger();

    private final OrderDAO orderDao = new OrderDAO();
    private final BillDAO billDao = new BillDAO();
    private final PaymentDAO paymentDao = new PaymentDAO();
    private final TableDAO tableDao = new TableDAO();
    private final CustomerDAO customerDao = new CustomerDAO();

    public List<Order> viewCompletedOrders() {
        List<Order> orders = orderDao.getOrdersByStatus("READY");
        return orders;
    }

    public List<OrderItem> getItemsForOrder(int orderId) {
        OrderItemDAO orderItemDao = new OrderItemDAO();
        List<OrderItem> orderItems = orderItemDao.getItemsByOrder(orderId);
        return orderItems;
    }

    public int generateBill(int orderId, double total) {
        int billId = billDao.generateBill(orderId, total);
        return billId;
    }

    public boolean recordPayment(int billId, String method, double amount) {
        String insertQuery = "INSERT INTO payments (bill_id, payment_method, amount) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(insertQuery)) {

            statement.setInt(1, billId);
            statement.setString(2, method);
            statement.setDouble(3, amount);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                int tableId = getTableIdFromBill(billId);
                if (tableId != -1) {
                    TableDAO tableService = new TableDAO();
                    tableService.freeTable(tableId);
                    LoggerUtil.grabLogger().info("bill " + billId + " paid and table " + tableId + " freed");
                }
                return true;
            }
        } catch (Exception exception) {
            LoggerUtil.grabLogger().severe("Error recording payment for bill " + billId + ": " + exception.getMessage());
        }
        return false;
    }

    public List<Table> viewVacantTables() {
        List<Table> tables = tableDao.getVacantTables();
        return tables;
    }

    public boolean freeTableManually(int tableId) {
        boolean tableFreed = tableDao.freeTable(tableId);
        boolean customerCleared = customerDao.clearCustomerByTableId(tableId);

        if (tableFreed && customerCleared) {
            QueueManager.getInstance().tryAssignFreeTable();
            logger.warning("table " + tableId + " was freed manually by manager");
            return true;
        }
        return false;
    }

    private int getTableIdFromBill(int billId) {
        String selectQuery = "SELECT o.table_id " +
                "FROM bills b " +
                "JOIN orders o ON b.order_id = o.order_id " +
                "WHERE b.bill_id = ?";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setInt(1, billId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("table_id");
            }
        } catch (Exception exception) {
            LoggerUtil.grabLogger().severe("Error fetching tableId from bill " + billId + ": " + exception.getMessage());
        }
        return -1;
    }
}