package com.restaurant.util;

import java.util.logging.*;

/**
 * very simple logger util. it writes logs to console and also to file.
 * not fancy but works.
 */
public class LoggerUtil {
    private static final Logger logger = Logger.getLogger("RestaurantLogs");

    static {
        try {
            FileHandler fileHandler = new FileHandler("restaurant-log.txt", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);

            logger.setLevel(Level.INFO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Logger grabLogger() {
        return logger;
    }
}
