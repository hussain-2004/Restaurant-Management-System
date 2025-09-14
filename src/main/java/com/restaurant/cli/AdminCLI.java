package com.restaurant.cli;

import com.restaurant.model.Admin;
import com.restaurant.service.AdminService;

/**
 * Administrator interface for complete system management including menu operations
 */
public class AdminCLI extends BaseCLI {
    private final AdminService adminService;

    public AdminCLI() {
        this.adminService = new AdminService();
    }

    public void showAdminMenu(Admin admin) {
        boolean shouldContinueLoop = true;
        while (shouldContinueLoop) {
            printAdminMenu(admin);
            int userChoice = getChoice("Select admin task (1-5): ");

            switch (userChoice) {
                case 1 -> viewAllMenuItems();
                case 2 -> addNewMenuItem();
                case 3 -> updateMenuItemPrice();
                case 4 -> deleteMenuItem();
                case 5 -> {
                    printInfo("Administrator session ended. System management complete, " + admin.getName() + "!");
                    shouldContinueLoop = false;
                }
                default -> printError("Invalid choice! Please select a number between 1-5.");
            }
        }
    }

    private void printAdminMenu(Admin admin) {
        printHeader("Administration Panel - " + admin.getName());
        System.out.println("1. View Complete Menu");
        System.out.println("   Display all menu items with current status");
        System.out.println();
        System.out.println("2. Add New Menu Item");
        System.out.println("   Create new dish and add to restaurant menu");
        System.out.println();
        System.out.println("3. Update Item Price");
        System.out.println("   Modify pricing for existing menu items");
        System.out.println();
        System.out.println("4. Remove Menu Item");
        System.out.println("   Delete item from restaurant menu");
        System.out.println();
        System.out.println("5. Exit Admin Panel");
        System.out.println("   Logout from administration system");
        System.out.println(DIVIDER);
    }

    private void viewAllMenuItems() {
        printSubHeader("Complete Restaurant Menu");

        var menuItems = adminService.seeAllMenuItems();
        if (menuItems.isEmpty()) {
            printWarning("No menu items found in the system. Consider adding some dishes!");
            waitForEnter();
            return;
        }

        System.out.printf("%-8s %-30s %-12s %-12s%n", "Menu ID", "Item Name", "Price (Rs)", "Status");
        System.out.println(SUB_DIVIDER);

        for (var menuItem : menuItems) {
            String availabilityStatus = menuItem.isAvailable() ? "Available" : "Unavailable";
            System.out.printf("%-8d %-30s Rs %-11.2f %-12s%n",
                    menuItem.getMenuId(),
                    menuItem.getItemName(),
                    menuItem.getPrice(),
                    availabilityStatus);
        }

        printInfo("Total menu items: " + menuItems.size());
        waitForEnter();
    }

    private void addNewMenuItem() {
        printSubHeader("Add New Menu Item");

        String itemName = getInput("Enter dish name: ");
        double itemPrice = getDoubleInput("Enter price (Rs): ");

        adminService.addMenuDish(itemName, itemPrice);
        printSuccess("New dish '" + itemName + "' added to menu successfully!");
        waitForEnter();
    }

    private void updateMenuItemPrice() {
        printSubHeader("Update Menu Item Price");

        // First show current menu for reference
        printInfo("Current menu items:");
        viewAllMenuItems();

        int menuId = getChoice("Enter Menu ID to update: ");
        double newPrice = getDoubleInput("Enter new price (Rs): ");

        adminService.changeDishPrice(menuId, newPrice);
        printSuccess("Menu item price updated successfully!");
        waitForEnter();
    }

    private void deleteMenuItem() {
        printSubHeader("Remove Menu Item");

        // Show current menu for reference
        printInfo("Current menu items:");
        viewAllMenuItems();

        int menuId = getChoice("Enter Menu ID to delete: ");

        // Confirmation prompt
        String userConfirmation = getInput("Are you sure you want to delete this item? (yes/no): ");
        if (!userConfirmation.equalsIgnoreCase("yes")) {
            printInfo("Delete operation cancelled.");
            waitForEnter();
            return;
        }

        adminService.deleteDish(menuId);
        printSuccess("Menu item deleted successfully!");
        waitForEnter();
    }
}