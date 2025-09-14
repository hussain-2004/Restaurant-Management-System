package com.restaurant.dao;

import com.restaurant.config.DatabaseConnection;
import com.restaurant.model.Customer;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CustomerDAOTest {

    private CustomerDAO customerDataAccessObjectForTesting;
    private Customer testCustomerInstance;
    private int testUserIdentifier;

    @BeforeAll
    void setupAll() {
        customerDataAccessObjectForTesting = new CustomerDAO();
    }

    @BeforeEach
    void setup() {
        try (Connection databaseConnectionForTestSetup = DatabaseConnection.fetchConnection()) {
            PreparedStatement userInsertionStatementForTesting = databaseConnectionForTestSetup.prepareStatement(
                    "INSERT INTO users (username, password, role) VALUES ('testuser', 'testpass', 'CUSTOMER') RETURNING user_id"
            );
            ResultSet userInsertionResultSetForTesting = userInsertionStatementForTesting.executeQuery();
            userInsertionResultSetForTesting.next();
            testUserIdentifier = userInsertionResultSetForTesting.getInt("user_id");

            PreparedStatement customerInsertionStatementForTesting = databaseConnectionForTestSetup.prepareStatement(
                    "INSERT INTO customers (user_id, name) VALUES (?, 'JUnit Customer') RETURNING customer_id"
            );
            customerInsertionStatementForTesting.setInt(1, testUserIdentifier);
            ResultSet customerInsertionResultSetForTesting = customerInsertionStatementForTesting.executeQuery();
            customerInsertionResultSetForTesting.next();
            int testCustomerIdentifier = customerInsertionResultSetForTesting.getInt("customer_id");

            testCustomerInstance = new Customer(testCustomerIdentifier, testUserIdentifier, "JUnit Customer");

        } catch (Exception testSetupException) {
            throw new RuntimeException("Error in test setup: " + testSetupException.getMessage(), testSetupException);
        }
    }

    @AfterEach
    void cleanup() {
        try (Connection databaseConnectionForTestCleanup = DatabaseConnection.fetchConnection()) {
            PreparedStatement customerDeletionStatementForCleanup = databaseConnectionForTestCleanup.prepareStatement("DELETE FROM customers WHERE user_id = ?");
            customerDeletionStatementForCleanup.setInt(1, testUserIdentifier);
            customerDeletionStatementForCleanup.executeUpdate();

            PreparedStatement userDeletionStatementForCleanup = databaseConnectionForTestCleanup.prepareStatement("DELETE FROM users WHERE user_id = ?");
            userDeletionStatementForCleanup.setInt(1, testUserIdentifier);
            userDeletionStatementForCleanup.executeUpdate();

        } catch (Exception cleanupException) {
            System.err.println("Cleanup failed: " + cleanupException.getMessage());
        }
    }

    @Test
    void assignTableToCustomer() {
        boolean tableAssignmentResult = customerDataAccessObjectForTesting.assignTableToCustomer(testCustomerInstance.getCustomerId(), 1);
        assertTrue(tableAssignmentResult);
    }

    @Test
    void clearTableForCustomer() {
        customerDataAccessObjectForTesting.assignTableToCustomer(testCustomerInstance.getCustomerId(), 1);
        boolean tableClearingResult = customerDataAccessObjectForTesting.clearTableForCustomer(testCustomerInstance.getCustomerId());
        assertTrue(tableClearingResult);
    }

    @Test
    void clearCustomerByTableId() {
        customerDataAccessObjectForTesting.assignTableToCustomer(testCustomerInstance.getCustomerId(), 1);
        boolean customerClearingByTableResult = customerDataAccessObjectForTesting.clearCustomerByTableId(1);
        assertTrue(customerClearingByTableResult);
    }

    @Test
    void getCustomerByUserId() {
        Customer retrievedCustomerFromDatabase = customerDataAccessObjectForTesting.getCustomerByUserId(testUserIdentifier);
        assertNotNull(retrievedCustomerFromDatabase);
        assertEquals(testCustomerInstance.getName(), retrievedCustomerFromDatabase.getName());
    }

    @Test
    void updateCheckInStatus() {
        boolean checkInStatusUpdateResult = customerDataAccessObjectForTesting.updateCheckInStatus(testCustomerInstance.getCustomerId(), true);
        assertTrue(checkInStatusUpdateResult);
    }
}