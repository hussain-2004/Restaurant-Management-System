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

    private final OrderDAO orderDataAccessObject = new OrderDAO();
    private final BillDAO billDataAccessObject = new BillDAO();
    private final PaymentDAO paymentDataAccessObject = new PaymentDAO();
    private final TableDAO tableDataAccessObject = new TableDAO();
    private final CustomerDAO customerDataAccessObject = new CustomerDAO();

    public List<Order> viewCompletedOrders() {
        List<Order> readyOrdersList = orderDataAccessObject.getOrdersByStatus("READY");
        return readyOrdersList;
    }

    public List<OrderItem> getItemsForOrder(int orderId) {
        OrderItemDAO orderItemDataAccessLayer = new OrderItemDAO();
        List<OrderItem> orderItemsForSpecificOrder = orderItemDataAccessLayer.getItemsByOrder(orderId);
        return orderItemsForSpecificOrder;
    }

    public int generateBill(int orderId, double total) {
        int generatedBillIdentifier = billDataAccessObject.generateBill(orderId, total);
        return generatedBillIdentifier;
    }

    public boolean recordPayment(int billId, String method, double amount) {
        String paymentInsertionQuery = "INSERT INTO payments (bill_id, payment_method, amount) VALUES (?, ?, ?)";
        try (Connection databaseConnectionForPayment = DatabaseConnection.fetchConnection();
             PreparedStatement paymentInsertionPreparedStatement = databaseConnectionForPayment.prepareStatement(paymentInsertionQuery)) {

            paymentInsertionPreparedStatement.setInt(1, billId);
            paymentInsertionPreparedStatement.setString(2, method);
            paymentInsertionPreparedStatement.setDouble(3, amount);
            int affectedRowsCount = paymentInsertionPreparedStatement.executeUpdate();

            if (affectedRowsCount > 0) {
                int associatedTableIdentifier = getTableIdFromBill(billId);
                if (associatedTableIdentifier != -1) {
                    TableDAO tableDataAccessForFreeing = new TableDAO();
                    tableDataAccessForFreeing.freeTable(associatedTableIdentifier);
                    LoggerUtil.grabLogger().info("bill " + billId + " paid and table " + associatedTableIdentifier + " freed");
                }
                return true;
            }
        } catch (Exception paymentRecordingException) {
            LoggerUtil.grabLogger().severe("Error recording payment for bill " + billId + ": " + paymentRecordingException.getMessage());
        }
        return false;
    }

    public List<Table> viewVacantTables() {
        List<Table> availableTablesList = tableDataAccessObject.getVacantTables();
        return availableTablesList;
    }

    public boolean freeTableManually(int tableId) {
        boolean tableFreedResult = tableDataAccessObject.freeTable(tableId);
        boolean customerClearedResult = customerDataAccessObject.clearCustomerByTableId(tableId);

        if (tableFreedResult && customerClearedResult) {
            QueueManager.getInstance().tryAssignFreeTable();
            logger.warning("table " + tableId + " was freed manually by manager");
            return true;
        }
        return false;
    }

    private int getTableIdFromBill(int billId) {
        String tableIdRetrievalQuery = "SELECT o.table_id " +
                "FROM bills b " +
                "JOIN orders o ON b.order_id = o.order_id " +
                "WHERE b.bill_id = ?";
        try (Connection databaseConnectionForTableRetrieval = DatabaseConnection.fetchConnection();
             PreparedStatement tableIdRetrievalPreparedStatement = databaseConnectionForTableRetrieval.prepareStatement(tableIdRetrievalQuery)) {

            tableIdRetrievalPreparedStatement.setInt(1, billId);
            ResultSet tableIdResultSet = tableIdRetrievalPreparedStatement.executeQuery();
            if (tableIdResultSet.next()) {
                return tableIdResultSet.getInt("table_id");
            }
        } catch (Exception tableIdRetrievalException) {
            LoggerUtil.grabLogger().severe("Error fetching tableId from bill " + billId + ": " + tableIdRetrievalException.getMessage());
        }
        return -1;
    }
}