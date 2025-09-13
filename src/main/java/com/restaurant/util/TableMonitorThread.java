package com.restaurant.util;

import com.restaurant.dao.CustomerDAO;
import com.restaurant.dao.TableDAO;
import com.restaurant.model.Customer;

/**
 * this thread waits 20 minutes. if the booked table is not checked in,
 * it will make table free again.
 */
public class TableMonitorThread extends Thread {
    private final Customer customer;
    private final int tableId;

    public TableMonitorThread(Customer customer, int tableId) {
        this.customer = customer;
        this.tableId = tableId;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(20 * 60 * 1000); // 20 min sleep
            if (!customer.isCheckedIn()) {
                new TableDAO().freeTable(tableId);
                new CustomerDAO().clearTableForCustomer(customer.getCustomerId());
                LoggerUtil.grabLogger().warning("customer " + customer.getName() + " not checked in. table " + tableId + " is freed");

                QueueManager.getInstance().tryAssignFreeTable();
            }
        } catch (InterruptedException e) {
            LoggerUtil.grabLogger().severe("error in table monitor thread: " + e.getMessage());
        }
    }
}
