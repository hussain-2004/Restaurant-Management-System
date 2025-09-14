package com.restaurant.cli;

import com.restaurant.exceptions.BookingException;
import com.restaurant.exceptions.OrderException;
import com.restaurant.model.Customer;
import com.restaurant.service.AdminService;
import com.restaurant.service.CustomerService;

/**
 * Customer interface for dining operations
 * Handles table booking, ordering, check-in, and billing
 */
public class CustomerCLI extends BaseCLI {
    private final CustomerService customerService;

    public CustomerCLI() {
        this.customerService = new CustomerService();
    }

    public void showCustomerMenu(Customer customer) {
        boolean shouldStayInMenu = true;
        while (shouldStayInMenu) {
            printCustomerMenu(customer);
            int userChoice = getChoice("Select your option (1-5): ");

            switch (userChoice) {
                case 1 -> handleTableBooking(customer);
                case 2 -> handleCheckIn(customer);
                case 3 -> handleOrderCreation(customer);
                case 4 -> handleBillGeneration(customer);
                case 5 -> {
                    printInfo("Thank you for dining with us, " + customer.getName() + "!");
                    shouldStayInMenu = false;
                }
                default -> printError("Invalid choice! Please select a number between 1-5.");
            }
        }
    }

    private void printCustomerMenu(Customer customer) {
        printHeader("Customer Dashboard - " + customer.getName());
        System.out.println("1. Table Reservation");
        System.out.println("   Reserve a table for your dining experience");
        System.out.println();
        System.out.println("2. Check-In");
        System.out.println("   Mark your arrival at the restaurant");
        System.out.println();
        System.out.println("3. Place Food Order");
        System.out.println("   Browse menu and place your order");
        System.out.println();
        System.out.println("4. Generate Bill");
        System.out.println("   Get your final bill for payment");
        System.out.println();
        System.out.println("5. Logout");
        System.out.println("   Sign out from your account");
        System.out.println(DIVIDER);
    }

    private void handleTableBooking(Customer customer) {
        printSubHeader("Table Reservation Service");
        int requiredSeats = getChoice("Number of guests (including yourself): ");

        try {
            String reservationMessage = customerService.bookTable(customer, requiredSeats);
            printSuccess(reservationMessage);
        } catch (BookingException bookingException) {
            printWarning(bookingException.getMessage());
        }
        waitForEnter();
    }

    private void handleCheckIn(Customer customer) {
        printSubHeader("Check-In Service");
        try {
            String checkInResult = customerService.checkIn(customer);
            printSuccess(checkInResult);
        } catch (BookingException bookingException) {
            printError(bookingException.getMessage());
        }
        waitForEnter();
    }

    private void handleOrderCreation(Customer customer) {
        printSubHeader("Food Ordering Service");
        try {
            int orderId = customerService.createOrder(customer);
            printSuccess("Order #" + orderId + " created! A waiter has been assigned to assist you.");

            // Display menu
            displayAvailableMenu();

            int menuId = getChoice("Enter Menu ID of the item you want: ");
            int quantity = getChoice("Enter quantity: ");

            customerService.addItemToOrder(orderId, menuId, quantity);
            printSuccess("Item added to your order successfully!");

        } catch (OrderException orderException) {
            printWarning(orderException.getMessage());
        }
        waitForEnter();
    }

    private void displayAvailableMenu() {
        printSubHeader("Available Menu Items");
        AdminService adminService = new AdminService();
        var menuItems = adminService.seeAllMenuItems();

        if (menuItems.isEmpty()) {
            printInfo("No menu items available at the moment.");
            return;
        }

        System.out.printf("%-5s %-30s %-10s%n", "ID", "Item Name", "Price");
        System.out.println(SUB_DIVIDER);

        for (var menuItem : menuItems) {
            if (menuItem.isAvailable()) {
                System.out.printf("%-5d %-30s Rs %-10.2f%n",
                        menuItem.getMenuId(),
                        menuItem.getItemName(),
                        menuItem.getPrice());
            }
        }
        System.out.println();
    }

    private void handleBillGeneration(Customer customer) {
        printSubHeader("Bill Generation Service");
        int billId = customerService.generateCombinedBill(customer);

        if (billId > 0) {
            printSuccess("Bill generated successfully! Bill ID: #" + billId);
            displayDetailedBill(customer);
        } else {
            printError("Unable to generate bill at this time. Please try again.");
        }
        waitForEnter();
    }

    private void displayDetailedBill(Customer customer) {
        printSubHeader("Your Detailed Bill");

        var orderItems = customerService.getAllOrderItemsForCustomer(
                customer.getCustomerId(), customer.getTableId());

        if (orderItems.isEmpty()) {
            printInfo("No items found for billing.");
            return;
        }

        System.out.printf("%-25s %-8s %-10s %-12s%n", "Item", "Qty", "Price", "Line Total");
        System.out.println(SUB_DIVIDER);

        double grandTotal = 0.0;
        for (var orderItem : orderItems) {
            double lineTotal = orderItem.getQuantity() * orderItem.getPrice();
            grandTotal += lineTotal;
            System.out.printf("%-25s %-8d Rs %-9.2f Rs %-11.2f%n",
                    orderItem.getItemName(),
                    orderItem.getQuantity(),
                    orderItem.getPrice(),
                    lineTotal);
        }

        System.out.println(SUB_DIVIDER);
        System.out.printf("%-43s Rs %-11.2f%n", "GRAND TOTAL:", grandTotal);
        System.out.println(DIVIDER);
    }
}