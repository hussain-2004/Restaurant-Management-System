package com.restaurant.model;

/**
 * admin is like super controller, can see reports and manage menu and staff list.
 */
public class Admin extends AbstractStaff {
    public Admin(int staffId, int userId, String name) {
        super(staffId, userId, name, "ADMIN");
    }

    @Override
    public void showMenu() {
        System.out.println("\n=== Admin Control Center ===");
        System.out.println("1. Check how much sales money came today.");
        System.out.println("2. Manage menu items like add new dish, update price or delete.");
        System.out.println("3. Look at list of all staff members with roles.");
        System.out.println("4. Logout from admin powers.");
    }
}
