package com.restaurant.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 * database connection.
 * It reads from properties file and prepare and returns a connection.
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private static String dbUrl;
    private static String dbUsername;
    private static String dbPassword;
    private static String dbDriver;

    private DatabaseConnection() {
        try (InputStream input = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            Properties properties = new Properties();
            properties.load(input);

            dbUrl = properties.getProperty("db.url");
            dbUsername = properties.getProperty("db.username");
            dbPassword = properties.getProperty("db.password");
            dbDriver = properties.getProperty("db.driver");

            Class.forName(dbDriver);
        } catch (IOException | ClassNotFoundException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public static Connection fetchConnection() {
        try {
            return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        } catch (Exception exception) {
            throw new RuntimeException("Some issue while connecting database: " + exception.getMessage());
        }
    }
}

