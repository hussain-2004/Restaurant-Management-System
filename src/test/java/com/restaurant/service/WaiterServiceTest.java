package com.restaurant.service;

import com.restaurant.config.DatabaseConnection;
import com.restaurant.model.Order;
import com.restaurant.model.OrderItem;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WaiterServiceTest {

    private static WaiterService waiterService;

    private static int waiterUserId;
    private static int waiterStaffId;
    private static int customerUserId;
    private static int customerId;
    private static int tableId;
    private static int orderId;
    private static int orderItemId;

    @BeforeAll
    static void setup() {
        waiterService = new WaiterService();

        try (Connection conn = DatabaseConnection.fetchConnection()) {

            // 1. Insert waiter user + staff
            PreparedStatement ps1 = conn.prepareStatement(
                    "INSERT INTO users (username, password, role) VALUES ('test_waiter', 'pass', 'WAITER') RETURNING user_id"
            );
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next()) waiterUserId = rs1.getInt("user_id");

            PreparedStatement ps2 = conn.prepareStatement(
                    "INSERT INTO staff (user_id, name, role) VALUES (?, 'Test Waiter', 'WAITER') RETURNING staff_id"
            );
            ps2.setInt(1, waiterUserId);
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) waiterStaffId = rs2.getInt("staff_id");

            // 2. Insert customer user + customer
            PreparedStatement ps3 = conn.prepareStatement(
                    "INSERT INTO users (username, password, role) VALUES ('test_customer', 'pass', 'CUSTOMER') RETURNING user_id"
            );
            ResultSet rs3 = ps3.executeQuery();
            if (rs3.next()) customerUserId = rs3.getInt("user_id");

            PreparedStatement ps4 = conn.prepareStatement(
                    "INSERT INTO customers (user_id, name) VALUES (?, 'Test Customer') RETURNING customer_id"
            );
            ps4.setInt(1, customerUserId);
            ResultSet rs4 = ps4.executeQuery();
            if (rs4.next()) customerId = rs4.getInt("customer_id");

            // 3. Insert table
            PreparedStatement ps5 = conn.prepareStatement(
                    "INSERT INTO tables (capacity, is_booked) VALUES (4, FALSE) RETURNING table_id"
            );
            ResultSet rs5 = ps5.executeQuery();
            if (rs5.next()) tableId = rs5.getInt("table_id");

            // 4. Insert order
            PreparedStatement ps6 = conn.prepareStatement(
                    "INSERT INTO orders (customer_id, table_id, waiter_id, status) VALUES (?, ?, ?, 'READY') RETURNING order_id"
            );
            ps6.setInt(1, customerId);
            ps6.setInt(2, tableId);
            ps6.setInt(3, waiterStaffId);
            ResultSet rs6 = ps6.executeQuery();
            if (rs6.next()) orderId = rs6.getInt("order_id");

            // 5. Insert order item
            PreparedStatement ps7 = conn.prepareStatement(
                    "INSERT INTO order_items (order_id, menu_id, quantity, status) VALUES (?, 1, 2, 'READY') RETURNING item_id"
            );
            ps7.setInt(1, orderId);
            ResultSet rs7 = ps7.executeQuery();
            if (rs7.next()) orderItemId = rs7.getInt("item_id");

        } catch (Exception e) {
            throw new RuntimeException("Test setup failed: " + e.getMessage(), e);
        }
    }

    @AfterAll
    static void cleanup() {
        try (Connection conn = DatabaseConnection.fetchConnection()) {
            conn.prepareStatement("DELETE FROM order_items WHERE item_id = " + orderItemId).executeUpdate();
            conn.prepareStatement("DELETE FROM orders WHERE order_id = " + orderId).executeUpdate();
            conn.prepareStatement("DELETE FROM tables WHERE table_id = " + tableId).executeUpdate();
            conn.prepareStatement("DELETE FROM customers WHERE customer_id = " + customerId).executeUpdate();
            conn.prepareStatement("DELETE FROM users WHERE user_id = " + customerUserId).executeUpdate();
            conn.prepareStatement("DELETE FROM staff WHERE staff_id = " + waiterStaffId).executeUpdate();
            conn.prepareStatement("DELETE FROM users WHERE user_id = " + waiterUserId).executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Test cleanup failed: " + e.getMessage(), e);
        }
    }

    @Test
    void getItemsByOrder() {
        List<OrderItem> items = waiterService.getItemsByOrder(orderId);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(orderId, items.get(0).getOrderId());
    }

    @Test
    void markOrderAsServed() {
        boolean updated = waiterService.markOrderAsServed(orderId);
        assertTrue(updated);
    }
}
