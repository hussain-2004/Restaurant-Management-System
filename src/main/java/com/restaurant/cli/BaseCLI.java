package com.restaurant.cli;

import java.util.Scanner;

/**
 * Base class for all CLI components providing common utilities and styling
 */
public abstract class BaseCLI {
    protected static final Scanner inputScanner = new Scanner(System.in);

    // Console styling constants
    protected static final String DIVIDER = "===============================================================";
    protected static final String SUB_DIVIDER = "---------------------------------------------------------------";
    protected static final String SUCCESS = "SUCCESS: ";
    protected static final String ERROR = "ERROR: ";
    protected static final String WARNING = "WARNING: ";
    protected static final String INFO = "INFO: ";
    protected static final String MENU_POINTER = "> ";

    protected void printHeader(String title) {
        System.out.println("\n" + DIVIDER);
        System.out.println("   " + title.toUpperCase());
        System.out.println(DIVIDER);
    }

    protected void printSubHeader(String title) {
        System.out.println("\n" + SUB_DIVIDER);
        System.out.println(" " + title);
        System.out.println(SUB_DIVIDER);
    }

    protected void printSuccess(String message) {
        System.out.println(SUCCESS + message);
    }

    protected void printError(String message) {
        System.out.println(ERROR + message);
    }

    protected void printWarning(String message) {
        System.out.println(WARNING + message);
    }

    protected void printInfo(String message) {
        System.out.println(INFO + message);
    }

    protected int getChoice(String prompt) {
        System.out.print(MENU_POINTER + prompt);
        return inputScanner.nextInt();
    }

    protected String getInput(String prompt) {
        System.out.print(MENU_POINTER + prompt);
        return inputScanner.next();
    }

    protected double getDoubleInput(String prompt) {
        System.out.print(MENU_POINTER + prompt);
        return inputScanner.nextDouble();
    }

    protected void waitForEnter() {
        System.out.println("\nPress Enter to continue...");
        inputScanner.nextLine();
        inputScanner.nextLine();
    }
}