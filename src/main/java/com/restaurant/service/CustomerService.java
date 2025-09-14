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
 * Manages customer operations including table booking, check-in, ordering, and billing.
 */
public class CustomerService {
    private static final Logger logger = LoggerUtil.grabLogger();

    private final TableDAO tableDao = new TableDAO();
    private final CustomerDAO customerDao = new CustomerDAO();
    private final OrderDAO orderDao = new OrderDAO();
    private final OrderItemDAO orderItemDao = new OrderItemDAO();
    private final BillDAO billDao = new BillDAO();

    public String bookTable(Customer customer, int requiredSeats) throws BookingException {
        if (customer.getTableId() != null) {
            logger.warning("customer " + customer.getName() + " tried double booking");
            throw new BookingException("You already have a table reserved.");
        }

        var availableTable = tableDao.getAvailableTable(requiredSeats);
        if (availableTable == null) {
            QueueManager.getInstance().putCustomerInQueue(customer);
            return "Sorry no free tables now, but you are placed in waiting line.";
        }

        boolean tableBooked = tableDao.assignTable(availableTable.getTableId());
        boolean customerLinked = customerDao.assignTableToCustomer(customer.getCustomerId(), availableTable.getTableId());

        if (tableBooked && customerLinked) {
            customer.setTableId(availableTable.getTableId());

            logger.info("table " + availableTable.getTableId() + " booked for customer " + customer.getName());

            TableMonitorThread monitor = new TableMonitorThread(customer, availableTable.getTableId());
            monitor.start();

            return "Table " + availableTable.getTableId() + " booked successfully for " + requiredSeats + " people. Please check in soon.";
        } else {
            throw new BookingException("Booking failed due to internal problem.");
        }
    }

    public String checkIn(Customer customer) throws BookingException {
        if (customer.getTableId() == null) {
            throw new BookingException("No booking found for you, please book table first.");
        }
        boolean checkInUpdated = customerDao.updateCheckInStatus(customer.getCustomerId(), true);

        if (checkInUpdated) {
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
        int orderId = orderDao.createOrder(customer.getCustomerId(), customer.getTableId(), waiterId);
        if (orderId == -1) {
            throw new OrderException("Order could not be created.");
        }
        return orderId;
    }

    public boolean addItemToOrder(int orderId, int menuId, int quantity) throws OrderException {
        boolean itemAdded = orderItemDao.addItemToOrder(orderId, menuId, quantity);
        if (!itemAdded) {
            throw new OrderException("Item could not be added to order.");
        }
        return true;
    }

    public int generateBill(int orderId, double totalAmount) {
        int billId = billDao.generateBill(orderId, totalAmount);
        return billId;
    }

    public int generateCombinedBill(Customer customer) {
        int billId = billDao.generateCombinedBill(customer.getCustomerId(), customer.getTableId());
        return billId;
    }

    public int createOrder(Customer customer) throws OrderException {
        if (customer.getTableId() == null) {
            throw new OrderException("You don't have a table, cannot place order.");
        }

        int waiterId = findAvailableWaiterId();
        if (waiterId == -1) {
            throw new OrderException("Sorry no waiter available right now.");
        }

        int orderId = orderDao.createOrder(customer.getCustomerId(), customer.getTableId(), waiterId);
        if (orderId == -1) {
            throw new OrderException("Order could not be created.");
        }
        return orderId;
    }

    private int findAvailableWaiterId() {
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT staff_id FROM staff WHERE role='WAITER' LIMIT 1");
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt("staff_id");
            }
        } catch (Exception exception) {
            LoggerUtil.grabLogger().severe("error finding waiter: " + exception.getMessage());
        }
        return -1;
    }

    public int getLatestOrderForCustomer(int customerId) {
        String selectQuery = "SELECT order_id FROM orders WHERE customer_id = ? ORDER BY order_id DESC LIMIT 1";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setInt(1, customerId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("order_id");
            }
        } catch (Exception exception) {
            LoggerUtil.grabLogger().severe("Error finding latest order: " + exception.getMessage());
        }
        return -1;
    }

    public double calculateOrderTotal(int orderId) {
        String selectQuery = "SELECT oi.quantity, m.price " +
                "FROM order_items oi " +
                "JOIN menu m ON oi.menu_id = m.menu_id " +
                "WHERE oi.order_id = ?";
        double totalAmount = 0.0;
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setInt(1, orderId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int quantity = resultSet.getInt("quantity");
                double price = resultSet.getDouble("price");
                totalAmount += quantity * price;
            }
        } catch (Exception exception) {
            LoggerUtil.grabLogger().severe("Error calculating total for order " + orderId + ": " + exception.getMessage());
        }
        return totalAmount;
    }

    public boolean registerCustomer(String name, String username, String password) {
        String insertUserQuery = "INSERT INTO users (username, password, role) VALUES (?, ?, 'CUSTOMER') RETURNING user_id";
        String insertCustomerQuery = "INSERT INTO customers (user_id, name) VALUES (?, ?)";

        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement userStatement = connection.prepareStatement(insertUserQuery);
        ) {
            userStatement.setString(1, username);
            userStatement.setString(2, password);
            ResultSet resultSet = userStatement.executeQuery();
            if (resultSet.next()) {
                int userId = resultSet.getInt("user_id");

                try (PreparedStatement customerStatement = connection.prepareStatement(insertCustomerQuery)) {
                    customerStatement.setInt(1, userId);
                    customerStatement.setString(2, name);
                    customerStatement.executeUpdate();
                }
                return true;
            }
        } catch (SQLException exception) {
            LoggerUtil.grabLogger().severe("Error registering customer: " + exception.getMessage());
            return false;
        }
        return false;
    }

    public List<OrderItem> getAllOrderItemsForCustomer(int customerId, int tableId) {
        List<OrderItem> orderItems = new ArrayList<>();
        String selectQuery = "SELECT oi.item_id, oi.order_id, oi.menu_id, m.item_name, m.price, oi.quantity, oi.status " +
                "FROM orders o " +
                "JOIN order_items oi ON o.order_id = oi.order_id " +
                "JOIN menu m ON oi.menu_id = m.menu_id " +
                "WHERE o.customer_id = ? AND o.table_id = ?";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            statement.setInt(1, customerId);
            statement.setInt(2, tableId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                OrderItem orderItem = new OrderItem(
                        resultSet.getInt("item_id"),
                        resultSet.getInt("order_id"),
                        resultSet.getInt("menu_id"),
                        resultSet.getInt("quantity"),
                        resultSet.getString("status")
                );
                orderItem.setItemName(resultSet.getString("item_name"));
                orderItem.setPrice(resultSet.getDouble("price"));
                orderItems.add(orderItem);
            }
        } catch (SQLException exception) {
            LoggerUtil.grabLogger().severe("Error fetching order items for bill: " + exception.getMessage());
        }
        return orderItems;
    }
}