package com.restaurant.util;

import com.restaurant.dao.CustomerDAO;
import com.restaurant.dao.TableDAO;
import com.restaurant.model.Customer;

import java.util.LinkedList;
import java.util.Queue;

/**
 * this manager keep track of customers waiting.
 * it is like a line infront of restaurant when no table free.
 * whenever table is freed we try to put first person in line to it.
 */
public class QueueManager {
    private static QueueManager onlyOneInstance;
    private final Queue<Customer> waitingCustomers = new LinkedList<>();
    private final TableDAO tableDao = new TableDAO();
    private final CustomerDAO customerDao = new CustomerDAO();

    private QueueManager() {}

    public static synchronized QueueManager getInstance() {
        if (onlyOneInstance == null) {
            onlyOneInstance = new QueueManager();
        }
        return onlyOneInstance;
    }

    public synchronized void putCustomerInQueue(Customer customer) {
        waitingCustomers.add(customer);
        LoggerUtil.grabLogger().info("customer " + customer.getName() + " added to waiting list");
    }

    public synchronized void tryAssignFreeTable() {
        if (waitingCustomers.isEmpty()) {
            return;
        }

        Customer firstPerson = waitingCustomers.peek();
        var someTable = tableDao.getAvailableTable();

        if (someTable != null && firstPerson != null) {
            boolean booked = tableDao.assignTable(someTable.getTableId());
            boolean linked = customerDao.assignTableToCustomer(firstPerson.getCustomerId(), someTable.getTableId());

            if (booked && linked) {
                waitingCustomers.poll();
                firstPerson.setTableId(someTable.getTableId());
                LoggerUtil.grabLogger().info("auto assigned table " + someTable.getTableId() + " to " + firstPerson.getName() + " from queue");
            }
        }
    }
}
