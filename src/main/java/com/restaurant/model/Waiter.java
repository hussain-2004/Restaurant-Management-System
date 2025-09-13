package com.restaurant.model;

/**
 * waiter is staff who bring food to table and see orders of customers.
 */
public class Waiter extends AbstractStaff {
    public Waiter(int staffId, int userId, String name) {
        super(staffId, userId, name, "WAITER");
    }

    @Override
    public void showMenu() {
        System.out.println("\n=== Dear Waiter, here is your working menu ===");
        System.out.println("1. See tables and their pending orders of customers.");
        System.out.println("2. After dish is ready, bring it to customer and mark as served.");
        System.out.println("3. Logout when you are done for the day.");
    }
}
