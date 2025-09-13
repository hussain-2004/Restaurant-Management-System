package com.restaurant;

import java.util.Scanner;

public class Main {

    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean exit = false;

        while (!exit) {
            System.out.println("\n=== Welcome to Restaurant Management System ===");
            System.out.println("1. Existing Customer Login");
            System.out.println("2. New Customer Registration");
            System.out.println("3. Staff/Admin Login");
            System.out.println("4. Exit");
            System.out.print("Enter choice: ");

            int choice = getIntInput();

            switch (choice) {
                case 1:
                    existingCustomerLogin();
                    break;
                case 2:
                    newCustomerRegistration();
                    break;
                case 3:
                    staffAdminLogin();
                    break;
                case 4:
                    System.out.println("Exiting... Thank you!");
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // Stub methods (we’ll implement later)
    private static void existingCustomerLogin() {
        com.restaurant.service.CustomerService service = new com.restaurant.service.CustomerService();
        com.restaurant.model.Customer customer = service.login();

        if (customer != null) {
            System.out.println("You are logged in as Customer ID: " + customer.getCustomerId());
            service.customerMenu(customer); // ✅ Show customer menu
        }
    }



    private static void newCustomerRegistration() {
        System.out.println("\n[New Customer Registration]");
        com.restaurant.service.CustomerService service = new com.restaurant.service.CustomerService();
        service.registerNewCustomer();
    }


    private static void staffAdminLogin() {
        com.restaurant.service.StaffService service = new com.restaurant.service.StaffService();
        service.login();
    }

    // Utility method to safely get integer input
    private static int getIntInput() {
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a number:");
            scanner.next();
        }
        return scanner.nextInt();
    }
}
