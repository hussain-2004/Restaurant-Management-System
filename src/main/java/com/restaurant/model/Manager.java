package com.restaurant.model;

/**
 * manager is like big boss who handle bills and table free or not free matters.
 */
public class Manager extends AbstractStaff {
    public Manager(int staffId, int userId, String name) {
        super(staffId, userId, name, "MANAGER");
    }

    @Override
    public void showMenu() {
        System.out.println("\n=== Manager Dashboard ===");
        System.out.println("1. View orders that are ready and need billing.");
        System.out.println("2. Create bill for customer order.");
        System.out.println("3. Accept payment and mark table free.");
        System.out.println("4. See vacant tables right now.");
        System.out.println("5. Free table manually if something went wrong.");
        System.out.println("6. Logout gracefully.");
    }
}
