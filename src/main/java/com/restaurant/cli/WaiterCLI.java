package com.restaurant.cli;

import com.restaurant.model.Waiter;
import com.restaurant.service.WaiterService;

/**
 * Waiter interface for managing assigned tables and orders
 */
public class WaiterCLI extends BaseCLI {
    private final WaiterService waiterService;

    public WaiterCLI() {
        this.waiterService = new WaiterService();
    }

    public void showWaiterMenu(Waiter waiter) {
        boolean shouldContinueLoop = true;
        while (shouldContinueLoop) {
            printWaiterMenu(waiter);
            int userChoice = getChoice("Select your task (1-3): ");

            switch (userChoice) {
                case 1 -> viewAssignedTablesAndOrders(waiter);
                case 2 -> markOrderAsServed();
                case 3 -> {
                    printInfo("Thank you for your service today, " + waiter.getName() + "!");
                    shouldContinueLoop = false;
                }
                default -> printError("Invalid choice! Please select 1, 2, or 3.");
            }
        }
    }

    private void printWaiterMenu(Waiter waiter) {
        printHeader("Waiter Dashboard - " + waiter.getName());
        System.out.println("1. View My Assigned Tables and Orders");
        System.out.println("   Check all tables and orders under your care");
        System.out.println();
        System.out.println("2. Mark Order as Served");
        System.out.println("   Update order status after serving customers");
        System.out.println();
        System.out.println("3. End Shift and Logout");
        System.out.println("   Clock out and close your session");
        System.out.println(DIVIDER);
    }

    private void viewAssignedTablesAndOrders(Waiter waiter) {
        printSubHeader("Your Assigned Tables and Orders");

        var assignedOrders = waiterService.getOrdersForWaiter(waiter.getStaffId());
        if (assignedOrders.isEmpty()) {
            printInfo("No tables or orders currently assigned to you. Enjoy your break!");
            waitForEnter();
            return;
        }

        System.out.printf("%-10s %-10s %-12s %-20s%n", "Order ID", "Table ID", "Customer ID", "Items Status");
        System.out.println(SUB_DIVIDER);

        for (var order : assignedOrders) {
            System.out.printf("%-10d %-10d %-12d",
                    order.getOrderId(),
                    order.getTableId(),
                    order.getCustomerId());

            var orderItems = waiterService.getItemsByOrder(order.getOrderId());
            if (orderItems.isEmpty()) {
                System.out.println("No items ordered yet");
            } else {
                System.out.println();
                for (var orderItem : orderItems) {
                    System.out.printf("%30s Item #%-3d (Menu #%-3d) Qty: %-3d Status: %s%n",
                            "",
                            orderItem.getItemId(),
                            orderItem.getMenuId(),
                            orderItem.getQuantity(),
                            orderItem.getStatus());
                }
            }
        }
        waitForEnter();
    }

    private void markOrderAsServed() {
        printSubHeader("Mark Order as Served");
        int orderId = getChoice("Enter Order ID to mark as served: ");

        boolean isOperationSuccessful = waiterService.markOrderAsServed(orderId);
        if (isOperationSuccessful) {
            printSuccess("Order #" + orderId + " has been marked as served!");
        } else {
            printError("Could not update order status. Please verify the Order ID.");
        }
        waitForEnter();
    }
}