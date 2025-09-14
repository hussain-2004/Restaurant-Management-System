package com.restaurant.service;

import com.restaurant.config.DatabaseConnection;
import com.restaurant.model.AbstractStaff;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

class StaffServiceTest {

    private static StaffService staffService;
    private static int userId;
    private static int staffId;

    @BeforeAll
    static void setup() {
        staffService = new StaffService();

        try (Connection conn = DatabaseConnection.fetchConnection()) {
            // Insert test user
            PreparedStatement ps1 = conn.prepareStatement(
                    "INSERT INTO users (username, password, role) VALUES ('test_staff_user', 'pass', 'WAITER') RETURNING user_id"
            );
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next()) {
                userId = rs1.getInt("user_id");
            }

            // Insert staff linked to user
            PreparedStatement ps2 = conn.prepareStatement(
                    "INSERT INTO staff (user_id, name, role) VALUES (?, 'Test Staff', 'WAITER') RETURNING staff_id"
            );
            ps2.setInt(1, userId);
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) {
                staffId = rs2.getInt("staff_id");
            }

        } catch (Exception e) {
            throw new RuntimeException("Test setup failed: " + e.getMessage(), e);
        }
    }

    @AfterAll
    static void cleanup() {
        try (Connection conn = DatabaseConnection.fetchConnection()) {
            conn.prepareStatement("DELETE FROM staff WHERE staff_id = " + staffId).executeUpdate();
            conn.prepareStatement("DELETE FROM users WHERE user_id = " + userId).executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Test cleanup failed: " + e.getMessage(), e);
        }
    }

    @Test
    void findStaffByUser() {
        AbstractStaff staff = staffService.findStaffByUser(userId);
        assertNotNull(staff);
        assertEquals("Test Staff", staff.getName());
        assertEquals("WAITER", staff.getRole());
    }
}
