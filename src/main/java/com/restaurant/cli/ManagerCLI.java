package com.restaurant.cli;

import com.restaurant.model.Manager;
import com.restaurant.service.ManagerService;

/**
 * Manager interface for overseeing restaurant operations, billing, and table management
 */
public class ManagerCLI extends BaseCLI {
    private final ManagerService managerService;

    public ManagerCLI() {
        this.managerService = new ManagerService();
    }

    public void showManagerMenu(Manager manager) {
        boolean shouldContinueLoop = true;
        while (shouldContinueLoop) {
            printManagerMenu(manager);
            int userChoice = getChoice("Select management task (1-6): ");

            switch (userChoice) {
                case 1 -> viewCompletedOrders();
                case 2 -> generateBill();
                case 3 -> recordPayment();
                case 4 -> viewVacantTables();
                case 5 -> freeTable();
                case 6 -> {
                    printInfo("Management session ended. Great work today, " + manager.getName() + "!");
                    shouldContinueLoop = false;
                }
                default -> printError("Invalid choice! Please select a number between 1-6.");
            }
        }
    }

    private void printManagerMenu(Manager manager) {
        printHeader("Management Dashboard - " + manager.getName());
        System.out.println("1. View Completed Orders");
        System.out.println("   Review orders ready for billing");
        System.out.println();
        System.out.println("2. Generate Customer Bill");
        System.out.println("   Create bill for completed orders");
        System.out.println();
        System.out.println("3. Process Payment");
        System.out.println("   Record customer payment and finalize transaction");
        System.out.println();
        System.out.println("4. View Available Tables");
        System.out.println("   Check current table availability status");
        System.out.println();
        System.out.println("5. Free Table Manually");
        System.out.println("   Manually release table for new customers");
        System.out.println();
        System.out.println("6. End Management Session");
        System.out.println("   Logout from management dashboard");
        System.out.println(DIVIDER);
    }

    private void viewCompletedOrders() {
        printSubHeader("Completed Orders Ready for Billing");

        var completedOrders = managerService.viewCompletedOrders();
        if (completedOrders.isEmpty()) {
            printInfo("No completed orders pending billing at this time.");
            waitForEnter();
            return;
        }

        System.out.printf("%-10s %-12s %-10s %-30s%n", "Order ID", "Customer ID", "Table ID", "Order Items");
        System.out.println(SUB_DIVIDER);

        for (var order : completedOrders) {
            System.out.printf("%-10d %-12d %-10d%n",
                    order.getOrderId(),
                    order.getCustomerId(),
                    order.getTableId());

            var orderItems = managerService.getItemsForOrder(order.getOrderId());
            if (orderItems.isEmpty()) {
                System.out.println(String.format("%33s No items found", ""));
            } else {
                for (var orderItem : orderItems) {
                    System.out.printf("%33s Item #%-3d (Menu #%-3d) Qty: %-3d [%s]%n",
                            "",
                            orderItem.getItemId(),
                            orderItem.getMenuId(),
                            orderItem.getQuantity(),
                            orderItem.getStatus());
                }
            }
            System.out.println();
        }
        waitForEnter();
    }

    private void generateBill() {
        printSubHeader("Bill Generation Service");

        int orderId = getChoice("Enter Order ID for billing: ");
        double totalAmount = getDoubleInput("Enter total bill amount (Rs): ");

        int billId = managerService.generateBill(orderId, totalAmount);
        if (billId > 0) {
            printSuccess("Bill #" + billId + " generated successfully!");
        } else {
            printError("Bill generation failed. Please verify order details.");
        }
        waitForEnter();
    }

    private void recordPayment() {
        printSubHeader("Payment Processing Service");

        int billId = getChoice("Enter Bill ID: ");
        String paymentMethod = getInput("Payment method (CASH/CARD/UPI): ").toUpperCase();
        double paymentAmount = getDoubleInput("Enter payment amount (Rs): ");

        boolean isPaymentSuccessful = managerService.recordPayment(billId, paymentMethod, paymentAmount);
        if (isPaymentSuccessful) {
            printSuccess("Payment recorded successfully! Table has been freed for new customers.");
        } else {
            printError("Payment processing failed. Please verify bill details.");
        }
        waitForEnter();
    }

    private void viewVacantTables() {
        printSubHeader("Available Table Status");

        var vacantTables = managerService.viewVacantTables();
        if (vacantTables.isEmpty()) {
            printWarning("All tables are currently occupied.");
            waitForEnter();
            return;
        }

        System.out.printf("%-10s %-12s %-15s%n", "Table ID", "Capacity", "Status");
        System.out.println(SUB_DIVIDER);

        for (var table : vacantTables) {
            System.out.printf("%-10d %-12d %-15s%n",
                    table.getTableId(),
                    table.getCapacity(),
                    "Available");
        }

        printInfo("Total vacant tables: " + vacantTables.size());
        waitForEnter();
    }

    private void freeTable() {
        printSubHeader("Manual Table Release Service");

        int tableId = getChoice("Enter Table ID to free: ");
        managerService.freeTableManually(tableId);
        printSuccess("Table #" + tableId + " has been manually freed and is now available.");
        waitForEnter();
    }
}