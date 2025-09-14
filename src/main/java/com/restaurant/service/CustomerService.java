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

        var freeTable = tableDao.getAvailableTable(requiredSeats); // 🔹 pass seats
        if (freeTable == null) {
            QueueManager.getInstance().putCustomerInQueue(customer);
            return "Sorry no free tables now, but you are placed in waiting line.";
        }

        boolean booked = tableDao.assignTable(freeTable.getTableId());
        boolean linked = customerDao.assignTableToCustomer(customer.getCustomerId(), freeTable.getTableId());

        if (booked && linked) {
            customer.setTableId(freeTable.getTableId());
            logger.info("table " + freeTable.getTableId() + " booked for customer " + customer.getName());

            TableMonitorThread monitor = new TableMonitorThread(customer, freeTable.getTableId());
            monitor.start();

            return "Table " + freeTable.getTableId() + " booked successfully for " + requiredSeats + " people. Please check in soon.";
        } else {
            throw new BookingException("Booking failed due to internal problem.");
        }
    }


    public String checkIn(Customer customer) throws BookingException {
        if (customer.getTableId() == null) {
            throw new BookingException("No booking found for you, please book table first.");
        }
        boolean updated = customerDao.updateCheckInStatus(customer.getCustomerId(), true);
        if (updated) {
            customer.setCheckedIn(true);
            return "You are now checked in successfully.";
        } else {
            throw new BookingException("Checkin failed due to some system issue.");
        }
    }

    public int createOrder(Customer customer, int waiterId) throws OrderException {
        if (customer.getTableId() == null) {
            throw new OrderException("You don’t have a table, cannot place order.");
        }
        int orderId = orderDao.createOrder(customer.getCustomerId(), customer.getTableId(), waiterId);
        if (orderId == -1) {
            throw new OrderException("Order could not be created.");
        }
        return orderId;
    }

    public boolean addItemToOrder(int orderId, int menuId, int quantity) throws OrderException {
        boolean done = orderItemDao.addItemToOrder(orderId, menuId, quantity);
        if (!done) {
            throw new OrderException("Item could not be added to order.");
        }
        return true;
    }


    public int generateBill(int orderId, double totalAmount) {
        return billDao.generateBill(orderId, totalAmount);
    }

    // 🔹 New method for combined bill
    public int generateCombinedBill(Customer customer) {
        return billDao.generateCombinedBill(customer.getCustomerId(), customer.getTableId());
    }



    public int createOrder(Customer customer) throws OrderException {
        if (customer.getTableId() == null) {
            throw new OrderException("You don’t have a table, cannot place order.");
        }

        // 🔹 pick a waiter automatically
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

    // temporary simple waiter selection
    private int findAvailableWaiterId() {
        // for now, just pick the first waiter in staff table
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT staff_id FROM staff WHERE role='WAITER' LIMIT 1");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("staff_id");
            }
        } catch (Exception e) {
            LoggerUtil.grabLogger().severe("error finding waiter: " + e.getMessage());
        }
        return -1;
    }

    public int getLatestOrderForCustomer(int customerId) {
        String sql = "SELECT order_id FROM orders WHERE customer_id = ? ORDER BY order_id DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("order_id");
            }
        } catch (Exception e) {
            LoggerUtil.grabLogger().severe("Error finding latest order: " + e.getMessage());
        }
        return -1;
    }

    public double calculateOrderTotal(int orderId) {
        String sql = "SELECT oi.quantity, m.price " +
                "FROM order_items oi " +
                "JOIN menu m ON oi.menu_id = m.menu_id " +
                "WHERE oi.order_id = ?";
        double total = 0.0;
        try (Connection conn = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int qty = rs.getInt("quantity");
                double price = rs.getDouble("price");
                total += qty * price;
            }
        } catch (Exception e) {
            LoggerUtil.grabLogger().severe("Error calculating total for order " + orderId + ": " + e.getMessage());
        }
        return total;
    }

    public boolean registerCustomer(String name, String username, String password) {
        String insertUser = "INSERT INTO users (username, password, role) VALUES (?, ?, 'CUSTOMER') RETURNING user_id";
        String insertCustomer = "INSERT INTO customers (user_id, name) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.fetchConnection();
             PreparedStatement stmtUser = conn.prepareStatement(insertUser);
        ) {
            stmtUser.setString(1, username);
            stmtUser.setString(2, password);
            ResultSet rs = stmtUser.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("user_id");

                try (PreparedStatement stmtCustomer = conn.prepareStatement(insertCustomer)) {
                    stmtCustomer.setInt(1, userId);
                    stmtCustomer.setString(2, name);
                    stmtCustomer.executeUpdate();
                }
                return true;
            }
        } catch (SQLException e) {
            LoggerUtil.grabLogger().severe("Error registering customer: " + e.getMessage());
            return false;
        }
        return false;
    }

    public List<OrderItem> getAllOrderItemsForCustomer(int customerId, int tableId) {
        List<OrderItem> result = new ArrayList<>();
        String sql = "SELECT oi.item_id, oi.order_id, oi.menu_id, m.item_name, m.price, oi.quantity, oi.status " +
                "FROM orders o " +
                "JOIN order_items oi ON o.order_id = oi.order_id " +
                "JOIN menu m ON oi.menu_id = m.menu_id " +
                "WHERE o.customer_id = ? AND o.table_id = ?";
        try (Connection conn = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.setInt(2, tableId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                OrderItem item = new OrderItem(
                        rs.getInt("item_id"),
                        rs.getInt("order_id"),
                        rs.getInt("menu_id"),
                        rs.getInt("quantity"),
                        rs.getString("status")
                );
                item.setItemName(rs.getString("item_name"));
                item.setPrice(rs.getDouble("price")); // add price in OrderItem model
                result.add(item);
            }
        } catch (SQLException e) {
            LoggerUtil.grabLogger().severe("Error fetching order items for bill: " + e.getMessage());
        }
        return result;
    }


}
