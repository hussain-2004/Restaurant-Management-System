package com.restaurant.model;

/**
 * chef is cook master who see pending orders and make them ready.
 */
public class Chef extends AbstractStaff {
    public Chef(int staffId, int userId, String name) {
        super(staffId, userId, name, "CHEF");
    }

    @Override
    public void showMenu() {
        System.out.println("\n=== Chef Panel ===");
        System.out.println("1. Look at all orders waiting to be cooked.");
        System.out.println("2. Once food is made tasty, mark it as completed.");
        System.out.println("3. Logout from chef mode.");
    }
}
