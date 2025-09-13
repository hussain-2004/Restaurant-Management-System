package com.restaurant.model;

/**
 * physical table in restaurant where customers sit and eat.
 */
public class Table {
    private int tableId;
    private int capacity;
    private boolean booked;
    private String bookingTime;

    public Table(int tableId, int capacity, boolean booked, String bookingTime) {
        this.tableId = tableId;
        this.capacity = capacity;
        this.booked = booked;
        this.bookingTime = bookingTime;
    }

    public int getTableId() {
        return tableId;
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean isBooked() {
        return booked;
    }

    public String getBookingTime() {
        return bookingTime;
    }
}
