package com.restaurant.model;

/**
 * this is like a base frame for all types of users in restaurant.
 * whether customer or staff or admin, all are first a user with id and name.
 */
public abstract class AbstractUser {
    protected int userId;
    protected String name;

    public AbstractUser(int userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }
}
