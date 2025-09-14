package com.restaurant.service;

import com.restaurant.config.DatabaseConnection;
import com.restaurant.dao.*;
import com.restaurant.exceptions.BookingException;
import com.restaurant.exceptions.OrderException;
import com.restaurant.model.Customer;
import com.restaurant.model.OrderItem;
import com.restaurant.util.LoggerUtil;
import com.restaurant.util.QueueManager;
import com.restaurant.util.TableMonitorThread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * customer service handle what customers can do like booking table, checkin, order food and billing.
 */
public class CustomerService {
    private static final Logger logger = LoggerUtil.grabLogger();

    private final TableDAO tableDataAccessObject = new TableDAO();
    private final CustomerDAO customerDataAccessObject = new CustomerDAO();
    private final OrderDAO orderDataAccessObject = new OrderDAO();
    private final OrderItemDAO orderItemDataAccessObject = new OrderItemDAO();
    private final BillDAO billDataAccessObject = new BillDAO();

    public String bookTable(Customer customer, int requiredSeats) throws BookingException {
        if (customer.getTableId() != null) {
            logger.warning("customer " + customer.getName() + " tried double booking");
            throw new BookingException("You already have a table reserved.");
        }

        var availableTableForBooking = tableDataAccessObject.getAvailableTable(requiredSeats);
        if (availableTableForBooking == null) {
            QueueManager.getInstance().putCustomerInQueue(customer);
            return "Sorry no free tables now, but you are placed in waiting line.";
        }

        boolean tableBookingResult = tableDataAccessObject.assignTable(availableTableForBooking.getTableId());
        boolean customerTableLinkingResult = customerDataAccessObject.assignTableToCustomer(customer.getCustomerId(), availableTableForBooking.getTableId());

        if (tableBookingResult && customerTableLinkingResult) {
            customer.setTableId(availableTableForBooking.getTableId());

            logger.info("table " + availableTableForBooking.getTableId() + " booked for customer " + customer.getName());

            TableMonitorThread tableMonitoringThread = new TableMonitorThread(customer, availableTableForBooking.getTableId());
            tableMonitoringThread.start();

            return "Table " + availableTableForBooking.getTableId() + " booked successfully for " + requiredSeats + " people. Please check in soon.";
        } else {
            throw new BookingException("Booking failed due to internal problem.");
        }
    }

    public String checkIn(Customer customer) throws BookingException {
        if (customer.getTableId() == null) {
            throw new BookingException("No booking found for you, please book table first.");
        }
        boolean checkInUpdateResult = customerDataAccessObject.updateCheckInStatus(customer.getCustomerId(), true);


        if (checkInUpdateResult) {
            customer.setCheckedIn(true);
            return "You are now checked in successfully.";
        } else {
            throw new BookingException("Checkin failed due to some system issue.");
        }
    }

    public int createOrder(Customer customer, int waiterId) throws OrderException {
        if (customer.getTableId() == null) {
            throw new OrderException("You don't have a table, cannot place order.");
        }
        int createdOrderIdentifier = orderDataAccessObject.createOrder(customer.getCustomerId(), customer.getTableId(), waiterId);
        if (createdOrderIdentifier == -1) {
            throw new OrderException("Order could not be created.");
        }
        return createdOrderIdentifier;
    }

    public boolean addItemToOrder(int orderId, int menuId, int quantity) throws OrderException {
        boolean itemAdditionResult = orderItemDataAccessObject.addItemToOrder(orderId, menuId, quantity);
        if (!itemAdditionResult) {
            throw new OrderException("Item could not be added to order.");
        }
        return true;
    }

    public int generateBill(int orderId, double totalAmount) {
        int generatedBillIdentifier = billDataAccessObject.generateBill(orderId, totalAmount);
        return generatedBillIdentifier;
    }

    public int generateCombinedBill(Customer customer) {
        int combinedBillIdentifier = billDataAccessObject.generateCombinedBill(customer.getCustomerId(), customer.getTableId());
        return combinedBillIdentifier;
    }

    public int createOrder(Customer customer) throws OrderException {
        if (customer.getTableId() == null) {
            throw new OrderException("You don't have a table, cannot place order.");
        }

        int availableWaiterIdentifier = findAvailableWaiterId();
        if (availableWaiterIdentifier == -1) {
            throw new OrderException("Sorry no waiter available right now.");
        }

        int newOrderIdentifier = orderDataAccessObject.createOrder(customer.getCustomerId(), customer.getTableId(), availableWaiterIdentifier);
        if (newOrderIdentifier == -1) {
            throw new OrderException("Order could not be created.");
        }
        return newOrderIdentifier;
    }

    private int findAvailableWaiterId() {
        try (Connection databaseConnectionForWaiterSearch = DatabaseConnection.fetchConnection();
             PreparedStatement findWaiterPreparedStatement = databaseConnectionForWaiterSearch.prepareStatement("SELECT staff_id FROM staff WHERE role='WAITER' LIMIT 1");
             ResultSet waiterSearchResultSet = findWaiterPreparedStatement.executeQuery()) {

            if (waiterSearchResultSet.next()) {
                return waiterSearchResultSet.getInt("staff_id");
            }
        } catch (Exception waiterSearchException) {
            LoggerUtil.grabLogger().severe("error finding waiter: " + waiterSearchException.getMessage());
        }
        return -1;
    }

    public int getLatestOrderForCustomer(int customerId) {
        String selectLatestOrderQuery = "SELECT order_id FROM orders WHERE customer_id = ? ORDER BY order_id DESC LIMIT 1";
        try (Connection databaseConnectionForLatestOrder = DatabaseConnection.fetchConnection();
             PreparedStatement latestOrderPreparedStatement = databaseConnectionForLatestOrder.prepareStatement(selectLatestOrderQuery)) {

            latestOrderPreparedStatement.setInt(1, customerId);
            ResultSet latestOrderResultSet = latestOrderPreparedStatement.executeQuery();
            if (latestOrderResultSet.next()) {
                return latestOrderResultSet.getInt("order_id");
            }
        } catch (Exception latestOrderException) {
            LoggerUtil.grabLogger().severe("Error finding latest order: " + latestOrderException.getMessage());
        }
        return -1;
    }

    public double calculateOrderTotal(int orderId) {
        String orderTotalCalculationQuery = "SELECT oi.quantity, m.price " +
                "FROM order_items oi " +
                "JOIN menu m ON oi.menu_id = m.menu_id " +
                "WHERE oi.order_id = ?";
        double orderTotalAmount = 0.0;
        try (Connection databaseConnectionForOrderTotal = DatabaseConnection.fetchConnection();
             PreparedStatement orderTotalPreparedStatement = databaseConnectionForOrderTotal.prepareStatement(orderTotalCalculationQuery)) {

            orderTotalPreparedStatement.setInt(1, orderId);
            ResultSet orderItemsWithPricesResultSet = orderTotalPreparedStatement.executeQuery();
            while (orderItemsWithPricesResultSet.next()) {
                int itemQuantity = orderItemsWithPricesResultSet.getInt("quantity");
                double itemPrice = orderItemsWithPricesResultSet.getDouble("price");
                orderTotalAmount += itemQuantity * itemPrice;
            }
        } catch (Exception orderTotalCalculationException) {
            LoggerUtil.grabLogger().severe("Error calculating total for order " + orderId + ": " + orderTotalCalculationException.getMessage());
        }
        return orderTotalAmount;
    }

    public boolean registerCustomer(String name, String username, String password) {
        String insertUserQuery = "INSERT INTO users (username, password, role) VALUES (?, ?, 'CUSTOMER') RETURNING user_id";
        String insertCustomerQuery = "INSERT INTO customers (user_id, name) VALUES (?, ?)";

        try (Connection databaseConnectionForRegistration = DatabaseConnection.fetchConnection();
             PreparedStatement userInsertionPreparedStatement = databaseConnectionForRegistration.prepareStatement(insertUserQuery);
        ) {
            userInsertionPreparedStatement.setString(1, username);
            userInsertionPreparedStatement.setString(2, password);
            ResultSet userInsertionResultSet = userInsertionPreparedStatement.executeQuery();
            if (userInsertionResultSet.next()) {
                int createdUserIdentifier = userInsertionResultSet.getInt("user_id");

                try (PreparedStatement customerInsertionPreparedStatement = databaseConnectionForRegistration.prepareStatement(insertCustomerQuery)) {
                    customerInsertionPreparedStatement.setInt(1, createdUserIdentifier);
                    customerInsertionPreparedStatement.setString(2, name);
                    customerInsertionPreparedStatement.executeUpdate();
                }
                return true;
            }
        } catch (SQLException customerRegistrationException) {
            LoggerUtil.grabLogger().severe("Error registering customer: " + customerRegistrationException.getMessage());
            return false;
        }
        return false;
    }

    public List<OrderItem> getAllOrderItemsForCustomer(int customerId, int tableId) {
        List<OrderItem> customerOrderItemsList = new ArrayList<>();
        String selectAllOrderItemsForCustomerQuery = "SELECT oi.item_id, oi.order_id, oi.menu_id, m.item_name, m.price, oi.quantity, oi.status " +
                "FROM orders o " +
                "JOIN order_items oi ON o.order_id = oi.order_id " +
                "JOIN menu m ON oi.menu_id = m.menu_id " +
                "WHERE o.customer_id = ? AND o.table_id = ?";
        try (Connection databaseConnectionForOrderItems = DatabaseConnection.fetchConnection();
             PreparedStatement orderItemsSelectionPreparedStatement = databaseConnectionForOrderItems.prepareStatement(selectAllOrderItemsForCustomerQuery)) {

            orderItemsSelectionPreparedStatement.setInt(1, customerId);
            orderItemsSelectionPreparedStatement.setInt(2, tableId);
            ResultSet customerOrderItemsResultSet = orderItemsSelectionPreparedStatement.executeQuery();
            while (customerOrderItemsResultSet.next()) {
                OrderItem orderItemFromDatabase = new OrderItem(
                        customerOrderItemsResultSet.getInt("item_id"),
                        customerOrderItemsResultSet.getInt("order_id"),
                        customerOrderItemsResultSet.getInt("menu_id"),
                        customerOrderItemsResultSet.getInt("quantity"),
                        customerOrderItemsResultSet.getString("status")
                );
                orderItemFromDatabase.setItemName(customerOrderItemsResultSet.getString("item_name"));
                orderItemFromDatabase.setPrice(customerOrderItemsResultSet.getDouble("price"));
                customerOrderItemsList.add(orderItemFromDatabase);
            }
        } catch (SQLException orderItemsRetrievalException) {
            LoggerUtil.grabLogger().severe("Error fetching order items for bill: " + orderItemsRetrievalException.getMessage());
        }
        return customerOrderItemsList;
    }
}