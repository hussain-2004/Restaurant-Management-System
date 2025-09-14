package com.restaurant.cli;

import com.restaurant.dao.CustomerDAO;
import com.restaurant.dao.StaffDAO;
import com.restaurant.model.*;
import com.restaurant.service.CustomerService;
import com.restaurant.util.AuthHelper;

/**
 * Handles all authentication-related operations
 */
public class AuthCLI extends BaseCLI {

    public void handleExistingCustomerLogin() {
        printHeader("Customer Login Portal");
        System.out.println("Welcome back! Please enter your credentials to continue.");

        String username = getInput("Username: ");
        String password = getInput("Password: ");

        var authenticatedUser = AuthHelper.validateCustomer(username, password);
        if (authenticatedUser == null) {
            printError("Login failed! Please check your username and password.");
            waitForEnter();
            return;
        }

        CustomerDAO customerDataAccessObject = new CustomerDAO();
        Customer customer = customerDataAccessObject.getCustomerByUserId(authenticatedUser.getUserId());
        if (customer == null) {
            printError("Customer profile not found! Please register first.");
            waitForEnter();
            return;
        }

        printSuccess("Welcome back, " + customer.getName() + "!");

        CustomerCLI customerCommandLineInterface = new CustomerCLI();
        customerCommandLineInterface.showCustomerMenu(customer);
    }

    public void handleNewCustomerRegistration() {
        printHeader("New Customer Registration");
        System.out.println("Join our restaurant family! Create your account below:");

        String customerName = getInput("Your Full Name: ");
        String username = getInput("Choose Username: ");
        String password = getInput("Choose Password: ");

        CustomerService customerService = new CustomerService();
        boolean isRegistrationSuccessful = customerService.registerCustomer(customerName, username, password);

        if (isRegistrationSuccessful) {
            printSuccess("Welcome to our restaurant, " + customerName + "!");
            printInfo("Your account has been created successfully.");
            printInfo("You can now login using your username and password.");
        } else {
            printError("Registration failed! Please try again or contact support.");
        }
        waitForEnter();
    }

    public void handleStaffLogin() {
        printHeader("Staff and Admin Login Portal");
        System.out.println("Staff members, please authenticate to access your workspace.");

        String username = getInput("Staff Username: ");
        String password = getInput("Staff Password: ");

        var authenticatedUser = AuthHelper.validateStaff(username, password);
        if (authenticatedUser == null) {
            printError("Staff authentication failed! Please verify your credentials.");
            waitForEnter();
            return;
        }

        StaffDAO staffDataAccessObject = new StaffDAO();
        var staffMember = staffDataAccessObject.getStaffByUserId(authenticatedUser.getUserId());
        if (staffMember == null) {
            printError("Staff profile not found in the system!");
            waitForEnter();
            return;
        }

        // Route to appropriate staff interface based on role
        switch (staffMember) {
            case Waiter waiter -> {
                printSuccess("Welcome, Waiter " + waiter.getName() + "!");
                new WaiterCLI().showWaiterMenu(waiter);
            }
            case Chef chef -> {
                printSuccess("Welcome to the Kitchen, Chef " + chef.getName() + "!");
                new ChefCLI().showChefMenu(chef);
            }
            case Manager manager -> {
                printSuccess("Welcome, Manager " + manager.getName() + "!");
                new ManagerCLI().showManagerMenu(manager);
            }
            case Admin admin -> {
                printSuccess("Welcome, Administrator " + admin.getName() + "!");
                new AdminCLI().showAdminMenu(admin);
            }
            default -> printError("Unknown staff role! Please contact system administrator.");
        }
    }
}