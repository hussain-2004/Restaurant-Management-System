package com.restaurant.model;

/**
 * line item inside one order, like dish name and quantity.
 */
public class OrderItem {
    private int itemId;
    private int orderId;
    private int menuId;
    private int quantity;
    private String status;
    private String itemName;
    private double price;

    public OrderItem(int itemId, int orderId, int menuId, int quantity, String status) {
        this.itemId = itemId;
        this.orderId = orderId;
        this.menuId = menuId;
        this.quantity = quantity;
        this.status = status;
    }

    public int getItemId() {
        return itemId;
    }

    public int getOrderId() {
        return orderId;
    }

    public int getMenuId() {
        return menuId;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getStatus() {
        return status;
    }

    public String getItemName() {  // 🔹 getter for itemName
        return itemName;
    }

    public void setItemName(String itemName) {  // 🔹 setter for itemName
        this.itemName = itemName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
