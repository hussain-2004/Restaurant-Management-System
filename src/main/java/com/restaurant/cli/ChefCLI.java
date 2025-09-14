package com.restaurant.cli;

import com.restaurant.model.Chef;
import com.restaurant.service.ChefService;

/**
 * Chef interface for managing kitchen operations and order preparation
 */
public class ChefCLI extends BaseCLI {
    private final ChefService chefService;

    public ChefCLI() {
        this.chefService = new ChefService();
    }

    public void showChefMenu(Chef chef) {
        boolean shouldContinueLoop = true;
        while (shouldContinueLoop) {
            printChefMenu(chef);
            int userChoice = getChoice("Select your kitchen task (1-3): ");

            switch (userChoice) {
                case 1 -> viewPendingOrders();
                case 2 -> markItemCompleted();
                case 3 -> {
                    printInfo("Kitchen shift ended. Great cooking today, Chef " + chef.getName() + "!");
                    shouldContinueLoop = false;
                }
                default -> printError("Invalid choice! Please select 1, 2, or 3.");
            }
        }
    }

    private void printChefMenu(Chef chef) {
        printHeader("Kitchen Dashboard - Chef " + chef.getName());
        System.out.println("1. View Pending Kitchen Orders");
        System.out.println("   Check all orders waiting for preparation");
        System.out.println();
        System.out.println("2. Mark Food Item as Ready");
        System.out.println("   Update item status when cooking is complete");
        System.out.println();
        System.out.println("3. End Kitchen Shift");
        System.out.println("   Close kitchen session and logout");
        System.out.println(DIVIDER);
    }

    private void viewPendingOrders() {
        printSubHeader("Pending Kitchen Orders");

        var pendingOrders = chefService.getPendingOrders();
        if (pendingOrders.isEmpty()) {
            printInfo("Kitchen is all caught up! No pending orders at the moment.");
            waitForEnter();
            return;
        }

        System.out.printf("%-10s %-12s %-10s %-30s%n", "Order ID", "Customer ID", "Table ID", "Items to Prepare");
        System.out.println(SUB_DIVIDER);

        for (var order : pendingOrders) {
            System.out.printf("%-10d %-12d %-10d%n",
                    order.getOrderId(),
                    order.getCustomerId(),
                    order.getTableId());

            var orderItems = chefService.getItemsForOrder(order.getOrderId());
            if (orderItems.isEmpty()) {
                System.out.println(String.format("%33s No items to prepare", ""));
            } else {
                for (var orderItem : orderItems) {
                    System.out.printf("%33s Item #%-3d: %-20s Qty: %-3d [%s]%n",
                            "",
                            orderItem.getItemId(),
                            orderItem.getItemName(),
                            orderItem.getQuantity(),
                            orderItem.getStatus());
                }
            }
            System.out.println();
        }
        waitForEnter();
    }

    private void markItemCompleted() {
        printSubHeader("Mark Food Item as Ready");
        int itemId = getChoice("Enter Item ID to mark as completed: ");

        boolean isOperationSuccessful = chefService.markOrderItemCompleted(itemId);
        if (isOperationSuccessful) {
            printSuccess("Item #" + itemId + " marked as ready for serving!");
        } else {
            printError("Could not update item status. Please verify the Item ID.");
        }
        waitForEnter();
    }
}