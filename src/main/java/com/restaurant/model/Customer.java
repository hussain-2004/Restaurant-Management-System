package com.restaurant.model;

/**
 * this is for customer guys. they can book table, checkin, order food and pay.
 */
public class Customer extends AbstractUser {
    private int customerId;
    private Integer tableId;
    private boolean checkedIn;
    private int requiredSeats;

    public Customer(int customerId, int userId, String name) {
        super(userId, name);
        this.customerId = customerId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public Integer getTableId() {
        return tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }

    public boolean isCheckedIn() {
        return checkedIn;
    }

    public void setCheckedIn(boolean checkedIn) {
        this.checkedIn = checkedIn;
    }

    public int getRequiredSeats() {
        return requiredSeats;
    }

    public void setRequiredSeats(int requiredSeats) {
        this.requiredSeats = requiredSeats;
    }

}
