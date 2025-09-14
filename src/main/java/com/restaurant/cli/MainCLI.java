package com.restaurant.cli;

/**
 * Main entry point for the Restaurant Management System CLI
 * Provides the primary navigation menu for all user types
 */
public class MainCLI extends BaseCLI {
    private final AuthCLI authenticationCommandLineInterface;

    public MainCLI() {
        this.authenticationCommandLineInterface = new AuthCLI();
    }

    public void start() {
        printWelcomeBanner();

        boolean isRunning = true;
        while (isRunning) {
            showMainMenu();
            int userChoice = getChoice("Please select an option (1-4): ");

            switch (userChoice) {
                case 1 -> authenticationCommandLineInterface.handleExistingCustomerLogin();
                case 2 -> authenticationCommandLineInterface.handleNewCustomerRegistration();
                case 3 -> authenticationCommandLineInterface.handleStaffLogin();
                case 4 -> {
                    printFarewell();
                    isRunning = false;
                }
                default -> printError("Invalid choice! Please select a number between 1-4.");
            }
        }
    }

    private void printWelcomeBanner() {
        System.out.println("\n" + DIVIDER);
        System.out.println("   WELCOME TO RESTAURANT MANAGEMENT SYSTEM");
        System.out.println(DIVIDER);
        System.out.println("   Your one-stop solution for seamless dining experience");
        System.out.println(DIVIDER);
    }

    private void showMainMenu() {
        printHeader("Main Menu - Choose Your Access Level");
        System.out.println("1. Customer Login");
        System.out.println("   For returning customers with existing accounts");
        System.out.println();
        System.out.println("2. New Customer Registration");
        System.out.println("   Create your account to start dining with us");
        System.out.println();
        System.out.println("3. Staff and Admin Portal");
        System.out.println("   For waiters, chefs, managers, and administrators");
        System.out.println();
        System.out.println("4. Exit Application");
        System.out.println("   Close the restaurant management system");
        System.out.println(DIVIDER);
    }

    private void printFarewell() {
        printHeader("Thank You for Using Our System!");
        System.out.println("We hope you had a wonderful experience!");
        System.out.println("Looking forward to serving you again soon!");
        System.out.println("Contact us anytime for reservations and inquiries.");
        System.out.println(DIVIDER);
    }
}