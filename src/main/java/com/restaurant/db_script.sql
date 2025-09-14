-- Step 1: Create Database
CREATE DATABASE restaurant_management_db;

\c restaurant_management_db;

-- Step 2: Create Tables

-- Users table (base for all login users)
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) CHECK (role IN ('CUSTOMER','WAITER','CHEF','MANAGER','ADMIN')) NOT NULL
);

-- Customers table
CREATE TABLE customers (
    customer_id SERIAL PRIMARY KEY,
    user_id INT UNIQUE REFERENCES users(user_id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    table_id INT NULL,
    checked_in BOOLEAN DEFAULT FALSE
);

-- Staff table
CREATE TABLE staff (
    staff_id SERIAL PRIMARY KEY,
    user_id INT UNIQUE REFERENCES users(user_id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(20) CHECK (role IN ('WAITER','CHEF','MANAGER')) NOT NULL
);

-- Tables in restaurant
CREATE TABLE tables (
    table_id SERIAL PRIMARY KEY,
    capacity INT NOT NULL,
    is_booked BOOLEAN DEFAULT FALSE,
    booking_time TIMESTAMP NULL
);

-- Menu items
CREATE TABLE menu (
    menu_id SERIAL PRIMARY KEY,
    item_name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL
);

-- Orders table
CREATE TABLE orders (
    order_id SERIAL PRIMARY KEY,
    customer_id INT REFERENCES customers(customer_id) ON DELETE CASCADE,
    table_id INT REFERENCES tables(table_id) ON DELETE CASCADE,
    waiter_id INT REFERENCES staff(staff_id) ON DELETE SET NULL,
    status VARCHAR(20) CHECK (status IN ('PENDING','READY','SERVED','COMPLETED')) DEFAULT 'PENDING',
    order_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Order Items
CREATE TABLE order_items (
    item_id SERIAL PRIMARY KEY,
    order_id INT REFERENCES orders(order_id) ON DELETE CASCADE,
    menu_id INT REFERENCES menu(menu_id) ON DELETE CASCADE,
    quantity INT NOT NULL,
    status VARCHAR(20) CHECK (status IN ('PENDING','COOKING','READY','SERVED')) DEFAULT 'PENDING'
);

-- Bills
CREATE TABLE bills (
    bill_id SERIAL PRIMARY KEY,
    order_id INT REFERENCES orders(order_id) ON DELETE CASCADE,
    total_amount DECIMAL(10,2) NOT NULL,
    is_paid BOOLEAN DEFAULT FALSE
);

-- Payments
CREATE TABLE payments (
    payment_id SERIAL PRIMARY KEY,
    bill_id INT REFERENCES bills(bill_id) ON DELETE CASCADE,
    method VARCHAR(20) CHECK (method IN ('CASH','CARD','UPI')) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Step 3: Insert Sample Data

-- Admin User
INSERT INTO users (username, password, role)
VALUES ('admin', 'admin123', 'ADMIN');

-- Staff Users
INSERT INTO users (username, password, role) VALUES
('waiter1', 'waiter123', 'WAITER'),
('chef1', 'chef123', 'CHEF'),
('manager1', 'manager123', 'MANAGER');

-- Link Staff
INSERT INTO staff (user_id, name, role) VALUES
(2, 'John Waiter', 'WAITER'),
(3, 'Alice Chef', 'CHEF'),
(4, 'Bob Manager', 'MANAGER');

-- Customers
INSERT INTO users (username, password, role) VALUES
('cust1', 'cust123', 'CUSTOMER'),
('cust2', 'cust123', 'CUSTOMER');

INSERT INTO customers (user_id, name) VALUES
(5, 'Hussain'),
(6, 'Sara');

-- Tables
INSERT INTO tables (capacity, is_booked) VALUES
(2, FALSE),
(4, FALSE),
(6, FALSE);

-- Menu
INSERT INTO menu (item_name, price) VALUES
('Pizza', 250.00),
('Burger', 150.00),
('Pasta', 200.00),
('Coke', 50.00),
('Coffee', 80.00);

