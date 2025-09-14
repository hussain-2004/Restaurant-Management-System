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
        var suitableTable = tableDao.getAvailableTable(firstPerson.getRequiredSeats()); // 🔹 use seats

        if (suitableTable != null && firstPerson != null) {
            boolean booked = tableDao.assignTable(suitableTable.getTableId());
            boolean linked = customerDao.assignTableToCustomer(firstPerson.getCustomerId(), suitableTable.getTableId());

            if (booked && linked) {
                waitingCustomers.poll();
                firstPerson.setTableId(suitableTable.getTableId());
                LoggerUtil.grabLogger().info(
                        "auto assigned table " + suitableTable.getTableId() +
                                " (capacity " + suitableTable.getCapacity() + ")" +
                                " to " + firstPerson.getName() +
                                " from queue (needed " + firstPerson.getRequiredSeats() + " seats)"
                );
            }
        }
    }

}
