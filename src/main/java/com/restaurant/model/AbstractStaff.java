package com.restaurant.model;

/**
 * staff peoples are special type of users who also got role.
 * waiter chef manager admin all belongs here.
 */
public abstract class AbstractStaff extends AbstractUser {
    protected int staffId;
    protected String role;

    public AbstractStaff(int staffId, int userId, String name, String role) {
        super(userId, name);
        this.staffId = staffId;
        this.role = role;
    }

    public int getStaffId() {
        return staffId;
    }

    public String getRole() {
        return role;
    }

    public abstract void showMenu();
}
