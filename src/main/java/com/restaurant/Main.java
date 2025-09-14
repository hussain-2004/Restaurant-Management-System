package com.restaurant;

import com.restaurant.dao.CustomerDAO;
import com.restaurant.dao.StaffDAO;
import com.restaurant.exceptions.BookingException;
import com.restaurant.exceptions.OrderException;
import com.restaurant.model.*;
import com.restaurant.service.*;
import com.restaurant.util.AuthHelper;

import java.util.List;
import java.util.Scanner;

/**
 * the entrypoint of our restaurant app. it is just text menus but long and descriptive.
 * every type of user can login here and perform things.
 */
public class Main {
    private static final Scanner inputScanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean running = true;
        while (running) {
            System.out.println("\n=== Welcome to The Restaurant Management System ===");
            System.out.println("Here you can either login as customer or staff or admin, or even create a new customer account.");
            System.out.println("Please select one of the below options carefully:");
            System.out.println("1. Existing Customer Login (if you already registered earlier)");
            System.out.println("2. New Customer Registration (for first timers)");
            System.out.println("3. Staff or Admin Login (for waiters, chefs, managers, admin)");
            System.out.println("4. Exit from this program gracefully");
            System.out.print("Your choice please: ");

            int choice = inputScanner.nextInt();
            switch (choice) {
                case 1 -> handleExistingCustomer();
                case 2 -> handleNewCustomer();
                case 3 -> handleStaffLogin();
                case 4 -> {
                    System.out.println("Thank you for visiting our restaurant system. Have a good day!");
                    running = false;
                }
                default -> System.out.println("Invalid choice, please try again with a valid number.");
            }
        }
    }

    private static void handleExistingCustomer() {
        System.out.println("\n--- Customer Login Page ---");
        System.out.print("Enter username: ");
        String username = inputScanner.next();
        System.out.print("Enter password: ");
        String password = inputScanner.next();

        var user = AuthHelper.validateCustomer(username, password);
        if (user == null) {
            System.out.println("Sorry, login failed. Username or password is wrong.");
            return;
        }

        CustomerDAO customerDao = new CustomerDAO();
        Customer customer = customerDao.getCustomerByUserId(user.getUserId());
        if (customer == null) {
            System.out.println("Sorry, no customer profile found. Please register first.");
            return;
        }

        System.out.println("Welcome back dear " + customer.getName() + "!");

        CustomerService customerService = new CustomerService();
        boolean stayInMenu = true;
        while (stayInMenu) {
            System.out.println("\n=== Customer Options Menu ===");
            System.out.println("1. Book a table in restaurant (if not already booked)");
            System.out.println("2. Do Check-In when you arrive physically");
            System.out.println("3. Call Waiter to give an Order (creates an order with waiter)");
            System.out.println("4. Generate Bill (after eating happily)");
            System.out.println("5. Logout from system");
            System.out.print("Select your option: ");

            int customerChoice = inputScanner.nextInt();
            switch (customerChoice) {
                case 1 -> {
                    System.out.print("How many people will be dining with you? ");
                    int requiredSeats = inputScanner.nextInt();
                    try {
                        String message = customerService.bookTable(customer, requiredSeats);
                        System.out.println(message);
                    } catch (BookingException e) {
                        System.out.println("⚠️ " + e.getMessage());
                    }
                }
                case 2 -> {
                    try {
                        String result = customerService.checkIn(customer);
                        System.out.println(result);
                    } catch (BookingException e) {
                        System.out.println(e.getMessage());
                    }
                }
                case 3 -> {
                    try {
                        int orderId = customerService.createOrder(customer);
                        System.out.println("✅ Order " + orderId + " created successfully and a waiter was assigned automatically.");

                        // 🔹 Show available menu items
                        AdminService adminService = new AdminService();
                        var items = adminService.seeAllMenuItems();
                        System.out.println("\n📖 Available Menu Items:");
                        for (var item : items) {
                            if (item.isAvailable()) {
                                System.out.println("   " + item.getMenuId() + ". " + item.getItemName() + " - ₹" + item.getPrice());
                            }
                        }

                        System.out.print("Enter menuId of item to add: ");
                        int menuId = inputScanner.nextInt();
                        System.out.print("Enter how many quantity: ");
                        int qty = inputScanner.nextInt();

                        customerService.addItemToOrder(orderId, menuId, qty);
                        System.out.println("🍽️ Item added to order successfully.");
                    } catch (OrderException e) {
                        System.out.println("⚠️ " + e.getMessage());
                    }
                }

                case 4 -> {
                    int billId = customerService.generateCombinedBill(customer);

                    if (billId > 0) {
                        System.out.println("✅ Combined Bill generated successfully! Bill ID: " + billId);
                        System.out.println("Here is your detailed bill:");

                        // Fetch all order items for this customer+table
                        var items = customerService.getAllOrderItemsForCustomer(customer.getCustomerId(), customer.getTableId());
                        double grandTotal = 0.0;

                        for (var item : items) {
                            double lineTotal = item.getQuantity() * item.getPrice(); // needs price from JOIN
                            grandTotal += lineTotal;
                            System.out.println(" - " + item.getItemName() +
                                    " x " + item.getQuantity() +
                                    " @ ₹" + item.getPrice() +
                                    " = ₹" + lineTotal);
                        }

                        System.out.println("----------------------------------");
                        System.out.println("Total Payable: ₹" + grandTotal);
                        System.out.println("----------------------------------");

                    } else {
                        System.out.println("❌ Could not generate bill.");
                    }
                }

                case 5 -> {
                    System.out.println("Logging out from customer account...");
                    stayInMenu = false;
                }
                default -> System.out.println("Invalid choice, please type again.");
            }
        }
    }

    private static void handleNewCustomer() {
        System.out.println("\n--- New Customer Registration Page ---");
        System.out.print("Please enter your name: ");
        String name = inputScanner.next();
        System.out.print("Choose username: ");
        String username = inputScanner.next();
        System.out.print("Choose password: ");
        String password = inputScanner.next();

        CustomerService customerService = new CustomerService();
        boolean success = customerService.registerCustomer(name, username, password);

        if (success) {
            System.out.println("✅ Welcome " + name + "! Your account is created. Please login now with your username and password.");
        } else {
            System.out.println("❌ Sorry, something went wrong. Your account could not be created.");
        }
    }


    private static void handleStaffLogin() {
        System.out.println("\n--- Staff or Admin Login ---");
        System.out.print("Enter username: ");
        String username = inputScanner.next();
        System.out.print("Enter password: ");
        String password = inputScanner.next();

        var user = AuthHelper.validateStaff(username, password);
        if (user == null) {
            System.out.println("Login failed for staff. Check username or password.");
            return;
        }

        StaffDAO staffDao = new StaffDAO();
        var staff = staffDao.getStaffByUserId(user.getUserId());
        if (staff == null) {
            System.out.println("Sorry, staff not found in system.");
            return;
        }

        if (staff instanceof Waiter) {
            handleWaiterMenu((Waiter) staff);
        } else if (staff instanceof Chef) {
            handleChefMenu((Chef) staff);
        } else if (staff instanceof Manager) {
            handleManagerMenu((Manager) staff);
        } else if (staff instanceof Admin) {
            handleAdminMenu((Admin) staff);
        }
    }

    private static void handleWaiterMenu(Waiter waiter) {
        WaiterService waiterService = new WaiterService();
        boolean loop = true;
        while (loop) {
            waiter.showMenu();
            int choice = inputScanner.nextInt();
            switch (choice) {
                case 1 -> {
                    var orders = waiterService.getOrdersForWaiter(waiter.getStaffId());
                    if (orders.isEmpty()) {
                        System.out.println("🪑 No tables or orders assigned to you at the moment.");
                    } else {
                        System.out.println("\n🪑 Orders and Tables you are responsible for:");
                        for (var order : orders) {
                            System.out.println("Order ID: " + order.getOrderId() +
                                    " | Table ID: " + order.getTableId() +
                                    " | Customer ID: " + order.getCustomerId());
                            var items = waiterService.getItemsByOrder(order.getOrderId());
                            if (items.isEmpty()) {
                                System.out.println("    (no items placed yet for this order)");
                            } else {
                                for (var item : items) {
                                    System.out.println("    Item ID: " + item.getItemId() +
                                            " | Menu ID: " + item.getMenuId() +
                                            " | Qty: " + item.getQuantity() +
                                            " | Status: " + item.getStatus());
                                }
                            }
                        }
                    }
                }
                case 2 -> {
                    System.out.print("Enter orderId to mark served: ");
                    int orderId = inputScanner.nextInt();
                    boolean ok = waiterService.markOrderAsServed(orderId);
                    System.out.println(ok ? "✅ Order marked as served." : "❌ Could not update order.");
                }
                case 3 -> {
                    System.out.println("👋 Waiter is logging out, enjoy your break!");
                    loop = false;
                }
                default -> System.out.println("Invalid option for waiter, please choose again.");
            }
        }
    }


    private static void handleChefMenu(Chef chef) {
        ChefService chefService = new ChefService();
        boolean loop = true;
        while (loop) {
            chef.showMenu();
            int choice = inputScanner.nextInt();
            switch (choice) {
                case 1 -> {
                    var pendingOrders = chefService.getPendingOrders();
                    if (pendingOrders.isEmpty()) {
                        System.out.println("🍳 No pending orders right now, kitchen is calm.");
                    } else {
                        System.out.println("\n🍳 Pending Orders in Kitchen:");
                        for (var order : pendingOrders) {
                            System.out.println("Order ID: " + order.getOrderId() +
                                    " | Customer ID: " + order.getCustomerId() +
                                    " | Table ID: " + order.getTableId());

                            var items = chefService.getItemsForOrder(order.getOrderId());
                            if (items.isEmpty()) {
                                System.out.println("    (no items added yet)");
                            } else {
                                for (var item : items) {
                                    System.out.println("    Item ID: " + item.getItemId() +
                                            " | " + item.getItemName() +
                                            " | Qty: " + item.getQuantity() +
                                            " | Status: " + item.getStatus());
                                }
                            }
                        }
                    }
                }
                case 2 -> {
                    System.out.print("Enter itemId to mark completed: ");
                    int itemId = inputScanner.nextInt();
                    boolean ok = chefService.markOrderItemCompleted(itemId);
                    System.out.println(ok ? "✅ Item marked completed." : "❌ Could not update item.");
                }
                case 3 -> {
                    System.out.println("👋 Chef is logging out of kitchen mode...");
                    loop = false;
                }
                default -> System.out.println("Invalid chef choice, please select a proper option.");
            }
        }
    }



    private static void handleManagerMenu(Manager manager) {
        ManagerService managerService = new ManagerService();
        boolean loop = true;
        while (loop) {
            manager.showMenu();
            int choice = inputScanner.nextInt();
            switch (choice) {
                case 1 -> {
                    var orders = managerService.viewCompletedOrders();
                    if (orders.isEmpty()) {
                        System.out.println("📋 No completed orders waiting for billing right now.");
                    } else {
                        System.out.println("\n📋 Completed Orders Ready for Billing:");
                        for (var order : orders) {
                            System.out.println("Order ID: " + order.getOrderId() +
                                    " | Customer ID: " + order.getCustomerId() +
                                    " | Table ID: " + order.getTableId());
                            var items = managerService.getItemsForOrder(order.getOrderId());
                            if (items.isEmpty()) {
                                System.out.println("    (no items found in this order)");
                            } else {
                                for (var item : items) {
                                    System.out.println("    Item ID: " + item.getItemId() +
                                            " | Menu ID: " + item.getMenuId() +
                                            " | Qty: " + item.getQuantity() +
                                            " | Status: " + item.getStatus());
                                }
                            }
                        }
                    }
                }
                case 2 -> {
                    System.out.print("Enter orderId to generate bill: ");
                    int orderId = inputScanner.nextInt();
                    System.out.print("Enter total bill amount: ");
                    double total = inputScanner.nextDouble();
                    int billId = managerService.generateBill(orderId, total);
                    System.out.println(billId > 0
                            ? "✅ Bill generated with ID " + billId
                            : "❌ Could not generate bill.");
                }
                case 3 -> {
                    System.out.print("Enter billId: ");
                    int billId = inputScanner.nextInt();
                    System.out.print("Enter payment method (CASH/CARD/UPI): ");
                    String method = inputScanner.next();
                    System.out.print("Enter amount: ");
                    double amt = inputScanner.nextDouble();
                    boolean ok = managerService.recordPayment(billId, method, amt);
                    System.out.println(ok ? "✅ Payment recorded and table freed." : "❌ Payment failed.");
                }
                case 4 -> {
                    var tables = managerService.viewVacantTables();
                    if (tables.isEmpty()) {
                        System.out.println("🪑 No vacant tables available at the moment.");
                    } else {
                        System.out.println("\n🪑 Vacant Tables:");
                        for (var table : tables) {
                            System.out.println("Table ID: " + table.getTableId() +
                                    " | Seats: " + table.getCapacity());
                        }
                    }
                }
                case 5 -> {
                    System.out.print("Enter tableId to manually free: ");
                    int tableId = inputScanner.nextInt();
                    managerService.freeTableManually(tableId);
                    System.out.println("Table " + tableId + " has been freed manually.");
                }
                case 6 -> {
                    System.out.println("👋 Manager is logging out...");
                    loop = false;
                }
                default -> System.out.println("Invalid manager choice, try again.");
            }
        }
    }


    private static void handleAdminMenu(Admin admin) {
        AdminService adminService = new AdminService();
        boolean loop = true;
        while (loop) {
            admin.showMenu();
            int choice = inputScanner.nextInt();
            switch (choice) {
                case 1 -> {
                    var items = adminService.seeAllMenuItems();
                    if (items.isEmpty()) {
                        System.out.println("📖 No menu items found in the system.");
                    } else {
                        System.out.println("\n📖 Current Menu Items:");
                        for (var item : items) {
                            System.out.println("Menu ID: " + item.getMenuId() +
                                    " | " + item.getItemName() +
                                    " | Price: ₹" + item.getPrice() +
                                    " | Available: " + (item.isAvailable() ? "Yes" : "No"));
                        }
                    }
                }
                case 2 -> {
                    System.out.print("Enter name of new dish: ");
                    String dish = inputScanner.next();
                    System.out.print("Enter price of " + dish + ": ");
                    double price = inputScanner.nextDouble();
                    adminService.addMenuDish(dish, price);
                    System.out.println("✅ Dish '" + dish + "' added successfully.");
                }
                case 3 -> {
                    System.out.print("Enter menuId of dish to update price: ");
                    int menuId = inputScanner.nextInt();
                    System.out.print("Enter new price: ");
                    double price = inputScanner.nextDouble();
                    adminService.changeDishPrice(menuId, price);
                    System.out.println("✅ Dish price updated successfully.");
                }
                case 4 -> {
                    System.out.print("Enter menuId of dish to delete: ");
                    int menuId = inputScanner.nextInt();
                    adminService.deleteDish(menuId);
                    System.out.println("✅ Dish deleted successfully.");
                }
                case 5 -> {
                    System.out.println("👋 Admin is logging out of the system...");
                    loop = false;
                }
                default -> System.out.println("Invalid admin choice, please try again.");
            }
        }
    }
}
