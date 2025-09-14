package com.restaurant.service;

import com.restaurant.config.DatabaseConnection;
import com.restaurant.model.MenuItem;
import com.restaurant.model.AbstractStaff;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdminServiceTest {

    private AdminService adminServiceInstanceForTesting;
    private int testMenuItemIdentifier;
    private int testStaffUserIdentifier;
    private int testStaffMemberIdentifier;

    @BeforeAll
    void setupAll() {
        adminServiceInstanceForTesting = new AdminService();
    }

    @BeforeEach
    void setup() {
        try (Connection databaseConnectionForTestSetup = DatabaseConnection.fetchConnection()) {
            PreparedStatement menuItemInsertionStatement = databaseConnectionForTestSetup.prepareStatement(
                    "INSERT INTO menu (item_name, price) VALUES ('JUnit Dish', 99.0) RETURNING menu_id"
            );

            ResultSet menuInsertionResultSet = menuItemInsertionStatement.executeQuery();
            menuInsertionResultSet.next();
            testMenuItemIdentifier = menuInsertionResultSet.getInt("menu_id");

            PreparedStatement staffUserInsertionStatement = databaseConnectionForTestSetup.prepareStatement(
                    "INSERT INTO users (username, password, role) VALUES ('junitstaff', 'test123', 'WAITER') RETURNING user_id"
            );
            ResultSet staffUserInsertionResultSet = staffUserInsertionStatement.executeQuery();
            staffUserInsertionResultSet.next();
            testStaffUserIdentifier = staffUserInsertionResultSet.getInt("user_id");

            PreparedStatement staffMemberInsertionStatement = databaseConnectionForTestSetup.prepareStatement(
                    "INSERT INTO staff (user_id, name, role) VALUES (?, 'JUnit Staff', 'WAITER') RETURNING staff_id"
            );
            staffMemberInsertionStatement.setInt(1, testStaffUserIdentifier);
            ResultSet staffInsertionResultSet = staffMemberInsertionStatement.executeQuery();
            staffInsertionResultSet.next();
            testStaffMemberIdentifier = staffInsertionResultSet.getInt("staff_id");

        } catch (Exception testSetupException) {
            throw new RuntimeException("Test setup failed: " + testSetupException.getMessage(), testSetupException);
        }
    }

    @AfterEach
    void cleanup() {
        try (Connection databaseConnectionForCleanup = DatabaseConnection.fetchConnection()) {
            PreparedStatement menuDeletionStatement = databaseConnectionForCleanup.prepareStatement("DELETE FROM menu WHERE menu_id = ?");
            menuDeletionStatement.setInt(1, testMenuItemIdentifier);
            menuDeletionStatement.executeUpdate();

            PreparedStatement staffDeletionStatement = databaseConnectionForCleanup.prepareStatement("DELETE FROM staff WHERE staff_id = ?");
            staffDeletionStatement.setInt(1, testStaffMemberIdentifier);
            staffDeletionStatement.executeUpdate();

            PreparedStatement userDeletionStatement = databaseConnectionForCleanup.prepareStatement("DELETE FROM users WHERE user_id = ?");
            userDeletionStatement.setInt(1, testStaffUserIdentifier);
            userDeletionStatement.executeUpdate();

        } catch (Exception cleanupException) {
            System.err.println("Cleanup failed: " + cleanupException.getMessage());
        }
    }

    @Test
    void seeAllMenuItems() {
        List<MenuItem> retrievedMenuItemsList = adminServiceInstanceForTesting.seeAllMenuItems();
        assertNotNull(retrievedMenuItemsList);
        assertTrue(retrievedMenuItemsList.stream().anyMatch(menuItemFromList -> menuItemFromList.getMenuId() == testMenuItemIdentifier));
    }

    @Test
    void addMenuDish() {
        boolean dishAdditionResult = adminServiceInstanceForTesting.addMenuDish("Extra JUnit Dish", 150.0);
        assertTrue(dishAdditionResult);

        try (Connection databaseConnectionForExtraCleanup = DatabaseConnection.fetchConnection()) {
            PreparedStatement extraDishDeletionStatement = databaseConnectionForExtraCleanup.prepareStatement("DELETE FROM menu WHERE name = 'Extra JUnit Dish'");
            extraDishDeletionStatement.executeUpdate();
        } catch (Exception extraCleanupException) {
        }
    }

    @Test
    void changeDishPrice() {
        boolean priceUpdateResult = adminServiceInstanceForTesting.changeDishPrice(testMenuItemIdentifier, 199.0);
        assertTrue(priceUpdateResult, "Dish price should be updated");

        try (Connection databaseConnectionForVerification = DatabaseConnection.fetchConnection()) {
            PreparedStatement priceVerificationStatement = databaseConnectionForVerification.prepareStatement("SELECT price FROM menu WHERE menu_id = ?");
            priceVerificationStatement.setInt(1, testMenuItemIdentifier);
            ResultSet priceVerificationResultSet = priceVerificationStatement.executeQuery();
            priceVerificationResultSet.next();
            assertEquals(199.0, priceVerificationResultSet.getDouble("price"));
        } catch (Exception priceVerificationException) {
            fail("Verification failed: " + priceVerificationException.getMessage());
        }
    }

    @Test
    void deleteDish() {
        boolean dishDeletionResult = adminServiceInstanceForTesting.deleteDish(testMenuItemIdentifier);
        assertTrue(dishDeletionResult, "Dish should be deleted");

        try (Connection databaseConnectionForDeletionVerification = DatabaseConnection.fetchConnection()) {
            PreparedStatement deletionVerificationStatement = databaseConnectionForDeletionVerification.prepareStatement("SELECT * FROM menu WHERE menu_id = ?");
            deletionVerificationStatement.setInt(1, testMenuItemIdentifier);
            ResultSet deletionVerificationResultSet = deletionVerificationStatement.executeQuery();
            assertFalse(deletionVerificationResultSet.next(), "Dish should not exist anymore");
        } catch (Exception deletionVerificationException) {
            fail("Verification failed: " + deletionVerificationException.getMessage());
        }
    }

    @Test
    void getStaffByUser() {
        AbstractStaff retrievedStaffMember = adminServiceInstanceForTesting.getStaffByUser(testStaffUserIdentifier);
        assertNotNull(retrievedStaffMember, "Staff should be fetched");
        assertEquals("JUnit Staff", retrievedStaffMember.getName());
    }
}