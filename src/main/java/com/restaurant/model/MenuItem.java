package com.restaurant.model;

/**
 * simple dish or drink item of menu with price and availability.
 */
public class MenuItem {
    private int menuId;
    private String itemName;
    private double price;
    private boolean available;

    public MenuItem(int menuId, String itemName, double price, boolean available) {
        this.menuId = menuId;
        this.itemName = itemName;
        this.price = price;
        this.available = available;
    }

    public int getMenuId() {
        return menuId;
    }

    public String getItemName() {
        return itemName;
    }

    public double getPrice() {
        return price;
    }

    public boolean isAvailable() {
        return available;
    }
}
