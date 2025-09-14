package com.restaurant;

import com.restaurant.cli.MainCLI;

/**
 * Main entry point for the Restaurant Management System
 * Delegates to the CLI package for user interface handling
 */
public class Main {
    public static void main(String[] args) {
        MainCLI mainCommandLineInterface = new MainCLI();
        mainCommandLineInterface.start();
    }
}