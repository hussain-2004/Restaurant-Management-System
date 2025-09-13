package com.restaurant.model;

/**
 * one customer order that has status like pending ready or served.
 */
public class Order {
    private int orderId;
    private int customerId;
    private int tableId;
    private int waiterId;
    private String status;
    private String orderTime;

    public Order(int orderId, int customerId, int tableId, int waiterId, String status, String orderTime) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.tableId = tableId;
        this.waiterId = waiterId;
        this.status = status;
        this.orderTime = orderTime;
    }

    public int getOrderId() {
        return orderId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public int getTableId() {
        return tableId;
    }

    public int getWaiterId() {
        return waiterId;
    }

    public String getStatus() {
        return status;
    }

    public String getOrderTime() {
        return orderTime;
    }
}
