---

# 🍽️ Restaurant Management System

A **console-based mini project** that simulates how a real-world restaurant works digitally. It handles everything from **customer bookings** to **chef cooking**, **waiter serving**, **billing**, and **payment management** — all with clean modular code, database integration, and role-based access.

---

## ✨ Features at a Glance

* 👤 **Customer Features**

    * Register & login
    * Book tables with seating preference
    * Place orders (waiter assigned automatically)
    * View menu, add items to order
    * Generate single or combined bills

* 👨‍🍳 **Chef Features**

    * View all pending orders with items & quantities
    * Mark items as cooked & ready

* 🧑‍🍽️ **Waiter Features**

    * View assigned customer orders
    * Mark items as served after delivery

* 🧑‍💼 **Manager Features**

    * Track vacant/occupied tables
    * Record payments & free tables

* 👨‍💻 **Admin Features**

    * Manage menu (add, update, delete dishes)
    * Manage staff (chefs, waiters, managers)
    * Access full system

---

## 🛠️ Technology Stack

* **Language:** Java 21
* **Database:** PostgreSQL
* **Build Tool:** Maven

---

## 📋 Prerequisites

Make sure the following are installed on your system:

* Java (>= 21)
* PostgreSQL (>= 14)
* Maven (>= 3.8)
* Git

---

## 🔧 Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/hussain-2004/Restaurant-Management-System
cd restaurant-management-system
```

### 2. Configure Database Connection

Update `src/main/resources/application.properties` with your PostgreSQL details:

```properties
db.url=jdbc:postgresql://127.0.0.1/restaurant_db   # replace with your database
db.username=<your-username>
db.password=<your-password>
db.driver=org.postgresql.Driver
```

---

## 🎯 Getting Started

### First Time Setup

* **Start the Application:** Run the `Main` class from your IDE or via Maven.
* **Database Initialization:** Run the provided schema file (`schema.sql`) to create required tables.
* **Default Admin Login:**

  ```
  Username: admin
  Password: admin123
  ```

### Basic Usage Flow

1. Login with admin credentials
2. Create new users and assign roles (chef, waiter, manager, customer)
3. Customers can register, login, and book tables
4. Place food orders → chef cooks → waiter serves
5. Bills are generated → manager records payment

---

## 👥 User Roles & Permissions

| Role         | Permissions                                                        |
| ------------ | ------------------------------------------------------------------ |
| **Admin**    | Full system access • Manage menu • Manage staff • Generate reports |
| **Manager**  | Record payments • Free tables • View table status                  |
| **Chef**     | View pending orders • Mark items cooked                            |
| **Waiter**   | View assigned orders • Mark items served                           |
| **Customer** | Register/login • Book tables • Place orders • View & pay bills     |

---

## 📊 Database Schema

### Core Tables

* **users** → login credentials & roles (admin, waiter, chef, manager, customer)
* **customers** → customer details linked to users
* **staff** → staff details linked to users
* **tables** → restaurant tables with booking status & capacity
* **menu** → list of dishes with prices
* **orders** → customer orders linked to table & waiter
* **order\_items** → individual items inside an order
* **bills** → generated bills for orders with payment status

---

## 📂 Project Structure


```plaintext
Restaurant-Management-System/
├── pom.xml                          # Maven build file
├── README.md                        # Project documentation
├── src/
│   ├── main/
│   │   ├── java/com/restaurant/
│   │   │   ├── config/              # Database & configuration
│   │   │   │   └── DatabaseConnection.java
│   │   │   │
│   │   │   ├── dao/                 # Data Access Layer
│   │   │   │   ├── BillDAO.java
│   │   │   │   ├── CustomerDAO.java
│   │   │   │   ├── MenuDAO.java
│   │   │   │   ├── OrderDAO.java
│   │   │   │   ├── OrderItemDAO.java
│   │   │   │   ├── StaffDAO.java
│   │   │   │   └── TableDAO.java
│   │   │   │
│   │   │   ├── enums/               # Enums for fixed constants
│   │   │   │   ├── OrderStatus.java
│   │   │   │   ├── PaymentStatus.java
│   │   │   │   └── Roles.java
│   │   │   │
│   │   │   ├── exceptions/          # Custom exceptions
│   │   │   │   └── DatabaseException.java
│   │   │   │
│   │   │   ├── model/               # Entity models
│   │   │   │   ├── Bill.java
│   │   │   │   ├── Customer.java
│   │   │   │   ├── Menu.java
│   │   │   │   ├── Order.java
│   │   │   │   ├── OrderItem.java
│   │   │   │   ├── Staff.java
│   │   │   │   └── Table.java
│   │   │   │
│   │   │   ├── service/             # Business logic
│   │   │   │   ├── AdminService.java
│   │   │   │   ├── BillService.java
│   │   │   │   ├── ChefService.java
│   │   │   │   ├── CustomerService.java
│   │   │   │   ├── OrderService.java
│   │   │   │   └── WaiterService.java
│   │   │   │
│   │   │   ├── service/interfaces/  # Contracts for services
│   │   │   │   ├── IAdminService.java
│   │   │   │   ├── IChefService.java
│   │   │   │   ├── ICustomerService.java
│   │   │   │   └── IWaiterService.java
│   │   │   │
│   │   │   ├── util/                # Utility classes
│   │   │   │   ├── LoggerUtil.java
│   │   │   │   ├── QueueManager.java
│   │   │   │   └── ValidationUtil.java
│   │   │   │
│   │   │   └── Main.java            # Application entry point
│   │   │
│   │   └── resources/
│   │       ├── application.properties  # DB configs
│   │       └── schema.sql              # Initial SQL schema
│   │
│   └── test/java/com/restaurant/
│       ├── dao/
│       │   └── CustomerDAOTest.java
│       │
│       ├── service/
│       │   ├── AdminServiceTest.java
│       │   ├── ChefServiceTest.java
│       │   ├── CustomerServiceTest.java
│       │   └── WaiterServiceTest.java
│       │
│       └── TestUtils.java             # Helpers for tests
│
└── .gitignore                        # Git ignore file
```

---

## 🚀 Future Enhancements

* Web-based version (Spring Boot + React)
* Payment gateway integration
* Advanced waiter allocation algorithm
* Analytics & reporting dashboard

---
