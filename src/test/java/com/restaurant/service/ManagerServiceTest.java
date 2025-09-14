package com.restaurant.service;

import com.restaurant.config.DatabaseConnection;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ManagerServiceTest {

    private ManagerService managerService;
    private int orderId;
    private int billId;
    private int tableId;
    private int customerId;

    @BeforeAll
    void setup() {
        managerService = new ManagerService();

        try (Connection conn = DatabaseConnection.fetchConnection()) {
            // Insert dummy user
            PreparedStatement ps1 = conn.prepareStatement(
                    "INSERT INTO users (username, password, role) VALUES ('test_manager_user', 'pass', 'CUSTOMER') RETURNING user_id"
            );
            ResultSet rs1 = ps1.executeQuery();
            int userId = -1;
            if (rs1.next()) {
                userId = rs1.getInt("user_id");
            }

            // Insert customer linked to user
            PreparedStatement ps2 = conn.prepareStatement(
                    "INSERT INTO customers (user_id, name, phone) VALUES (?, 'Test Customer', '9999999999') RETURNING customer_id"
            );
            ps2.setInt(1, userId);
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) {
                customerId = rs2.getInt("customer_id");
            }

            // Insert table
            PreparedStatement ps3 = conn.prepareStatement(
                    "INSERT INTO tables (capacity, is_booked) VALUES (4, FALSE) RETURNING table_id"
            );
            ResultSet rs3 = ps3.executeQuery();
            if (rs3.next()) {
                tableId = rs3.getInt("table_id");
            }

            // Insert order with PENDING status
            PreparedStatement ps4 = conn.prepareStatement(
                    "INSERT INTO orders (customer_id, table_id, status) VALUES (?, ?, 'PENDING') RETURNING order_id"
            );
            ps4.setInt(1, customerId);
            ps4.setInt(2, tableId);
            ResultSet rs4 = ps4.executeQuery();
            if (rs4.next()) {
                orderId = rs4.getInt("order_id");
            }

            // Add order item
            PreparedStatement ps5 = conn.prepareStatement(
                    "INSERT INTO order_items (order_id, menu_id, quantity, status) VALUES (?, 1, 2, 'PENDING')"
            );
            ps5.setInt(1, orderId);
            ps5.executeUpdate();

            // Simulate chef workflow: COMPLETED
            conn.prepareStatement("UPDATE order_items SET status = 'COMPLETED' WHERE order_id = " + orderId).executeUpdate();
            conn.prepareStatement("UPDATE orders SET status = 'READY' WHERE order_id = " + orderId).executeUpdate();
            conn.prepareStatement("UPDATE orders SET status = 'SERVED' WHERE order_id = " + orderId).executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Test setup failed: " + e.getMessage(), e);
        }
    }

    @AfterAll
    void cleanup() {
        try (Connection conn = DatabaseConnection.fetchConnection()) {
            conn.prepareStatement("DELETE FROM bills WHERE order_id = " + orderId).executeUpdate();
            conn.prepareStatement("DELETE FROM order_items WHERE order_id = " + orderId).executeUpdate();
            conn.prepareStatement("DELETE FROM orders WHERE order_id = " + orderId).executeUpdate();
            conn.prepareStatement("DELETE FROM tables WHERE table_id = " + tableId).executeUpdate();
            conn.prepareStatement("DELETE FROM customers WHERE customer_id = " + customerId).executeUpdate();
            conn.prepareStatement("DELETE FROM users WHERE username = 'test_manager_user'").executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Test cleanup failed: " + e.getMessage(), e);
        }
    }


    @Test
    void generateBill() {
        billId = managerService.generateBill(orderId, 200.0);
        assertTrue(billId > 0);
    }

    @Test
    void recordPayment() {
        billId = managerService.generateBill(orderId, 200.0);
        assertTrue(managerService.recordPayment(billId, "CASH", 200.0));
    }

    @Test
    void viewCompletedOrders() {
        assertNotNull(managerService.viewCompletedOrders());
    }

    @Test
    void getItemsForOrder() {
        assertNotNull(managerService.getItemsForOrder(orderId));
    }

    @Test
    void viewVacantTables() {
        assertNotNull(managerService.viewVacantTables());
    }
}
