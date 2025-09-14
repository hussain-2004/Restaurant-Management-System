package com.restaurant.dao;

import com.restaurant.config.DatabaseConnection;
import com.restaurant.model.Order;
import com.restaurant.service.interfaces.OrderServiceInterface;
import com.restaurant.util.LoggerUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * this dao is for orders, one order belongs to one customer, one table and a waiter
 * we can create new order, get orders, and change status.
 */
public class OrderDAO implements OrderServiceInterface {
    private static final Logger logger = LoggerUtil.grabLogger();

    @Override
    public int createOrder(int customerId, int tableId, int waiterId) {
        String insertNewOrderQuery = "INSERT INTO orders (customer_id, table_id, waiter_id, status) VALUES (?, ?, ?, 'PENDING') RETURNING order_id";
        try (Connection databaseConnection = DatabaseConnection.fetchConnection();
             PreparedStatement insertOrderStatement = databaseConnection.prepareStatement(insertNewOrderQuery)) {

            insertOrderStatement.setInt(1, customerId);
            insertOrderStatement.setInt(2, tableId);
            insertOrderStatement.setInt(3, waiterId);
            ResultSet newOrderIdResultSet = insertOrderStatement.executeQuery();

            if (newOrderIdResultSet.next()) {
                int generatedOrderId = newOrderIdResultSet.getInt("order_id");
                logger.info("new order " + generatedOrderId + " created for customer " + customerId);
                return generatedOrderId;
            }
        } catch (SQLException sqlException) {
            logger.severe("error creating order for customer " + customerId + ": " + sqlException.getMessage());
        }
        return -1;
    }

    @Override
    public List<Order> getOrdersByCustomer(int customerId) {
        List<Order> customerOrdersList = new ArrayList<>();
        String selectOrdersByCustomerQuery = "SELECT * FROM orders WHERE customer_id = ? ORDER BY order_time DESC";
        try (Connection databaseConnection = DatabaseConnection.fetchConnection();
             PreparedStatement selectCustomerOrdersStatement = databaseConnection.prepareStatement(selectOrdersByCustomerQuery)) {

            selectCustomerOrdersStatement.setInt(1, customerId);
            ResultSet customerOrdersResultSet = selectCustomerOrdersStatement.executeQuery();

            while (customerOrdersResultSet.next()) {
                customerOrdersList.add(new Order(customerOrdersResultSet.getInt("order_id"),
                        customerOrdersResultSet.getInt("customer_id"),
                        customerOrdersResultSet.getInt("table_id"),
                        customerOrdersResultSet.getInt("waiter_id"),
                        customerOrdersResultSet.getString("status"),
                        customerOrdersResultSet.getString("order_time")));
            }
        } catch (SQLException sqlException) {
            logger.warning("error fetching orders for customer " + customerId + ": " + sqlException.getMessage());
        }
        return customerOrdersList;
    }

    @Override
    public boolean updateOrderStatus(int orderId, String status) {
        String updateOrderStatusQuery = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (Connection databaseConnection = DatabaseConnection.fetchConnection();
             PreparedStatement updateOrderStatusStatement = databaseConnection.prepareStatement(updateOrderStatusQuery)) {

            updateOrderStatusStatement.setString(1, status);
            updateOrderStatusStatement.setInt(2, orderId);
            return updateOrderStatusStatement.executeUpdate() > 0;
        } catch (SQLException sqlException) {
            logger.warning("error updating status of order " + orderId + ": " + sqlException.getMessage());
            return false;
        }
    }

    public List<Order> getOrdersByStatus(String status) {
        List<Order> ordersWithSpecificStatusList = new ArrayList<>();
        String selectOrdersByStatusQuery = "SELECT * FROM orders WHERE status = ? ORDER BY order_time";
        try (Connection databaseConnection = DatabaseConnection.fetchConnection();
             PreparedStatement selectOrdersByStatusStatement = databaseConnection.prepareStatement(selectOrdersByStatusQuery)) {

            selectOrdersByStatusStatement.setString(1, status);
            ResultSet ordersWithStatusResultSet = selectOrdersByStatusStatement.executeQuery();

            while (ordersWithStatusResultSet.next()) {
                ordersWithSpecificStatusList.add(new Order(ordersWithStatusResultSet.getInt("order_id"),
                        ordersWithStatusResultSet.getInt("customer_id"),
                        ordersWithStatusResultSet.getInt("table_id"),
                        ordersWithStatusResultSet.getInt("waiter_id"),
                        ordersWithStatusResultSet.getString("status"),
                        ordersWithStatusResultSet.getString("order_time")));
            }
        } catch (SQLException sqlException) {
            logger.warning("error fetching orders by status " + status + ": " + sqlException.getMessage());
        }
        return ordersWithSpecificStatusList;
    }
}