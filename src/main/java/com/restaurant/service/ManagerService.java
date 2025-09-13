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
 * manager service is for the boss guy in restaurant,
 * he can see orders, make bills, take payments and free tables.
 */
public class ManagerService {
    private static final Logger logger = LoggerUtil.grabLogger();

    private final OrderDAO orderDao = new OrderDAO();
    private final BillDAO billDao = new BillDAO();
    private final PaymentDAO paymentDao = new PaymentDAO();
    private final TableDAO tableDao = new TableDAO();
    private final CustomerDAO customerDao = new CustomerDAO();

    public List<Order> viewCompletedOrders() {
        return orderDao.getOrdersByStatus("READY");
    }

    public List<OrderItem> getItemsForOrder(int orderId) {
        return new OrderItemDAO().getItemsByOrder(orderId);
    }


    public int generateBill(int orderId, double total) {
        return billDao.generateBill(orderId, total);
    }

    public boolean recordPayment(int billId, String method, double amount) {
        String sql = "INSERT INTO payments (bill_id, payment_method, amount) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, billId);
            stmt.setString(2, method);
            stmt.setDouble(3, amount);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                int tableId = getTableIdFromBill(billId);
                if (tableId != -1) {
                    new TableDAO().freeTable(tableId);
                    LoggerUtil.grabLogger().info("bill " + billId + " paid and table " + tableId + " freed");
                }
                return true;
            }
        } catch (Exception e) {
            LoggerUtil.grabLogger().severe("Error recording payment for bill " + billId + ": " + e.getMessage());
        }
        return false;
    }


    public List<Table> viewVacantTables() {
        return tableDao.getVacantTables();
    }

    public boolean freeTableManually(int tableId) {
        boolean freed = tableDao.freeTable(tableId);
        boolean cleared = customerDao.clearCustomerByTableId(tableId);
        if (freed && cleared) {
            QueueManager.getInstance().tryAssignFreeTable();
            logger.warning("table " + tableId + " was freed manually by manager");
            return true;
        }
        return false;
    }

    private int getTableIdFromBill(int billId) {
        String sql = "SELECT o.table_id " +
                "FROM bills b " +
                "JOIN orders o ON b.order_id = o.order_id " +
                "WHERE b.bill_id = ?";
        try (Connection conn = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, billId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("table_id");
            }
        } catch (Exception e) {
            LoggerUtil.grabLogger().severe("Error fetching tableId from bill " + billId + ": " + e.getMessage());
        }
        return -1;
    }

}
