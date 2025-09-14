package com.restaurant.dao;

import com.restaurant.config.DatabaseConnection;
import com.restaurant.model.MenuItem;
import com.restaurant.service.interfaces.MenuServiceInterface;
import com.restaurant.util.LoggerUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Handles menu item operations including retrieval and administrative updates.
 */
public class MenuDAOInterface implements MenuServiceInterface {
    private static final Logger logger = LoggerUtil.grabLogger();

    @Override
    public List<MenuItem> getAllMenuItems() {
        List<MenuItem> menuItems = new ArrayList<>();
        String selectQuery = "SELECT * FROM menu ORDER BY menu_id";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                menuItems.add(new MenuItem(resultSet.getInt("menu_id"),
                        resultSet.getString("item_name"),
                        resultSet.getDouble("price"),
                        resultSet.getBoolean("is_available")));
            }
        } catch (SQLException exception) {
            logger.warning("error reading all menu items: " + exception.getMessage());
        }
        return menuItems;
    }

    @Override
    public boolean addMenuItem(String name, double price) {
        String insertQuery = "INSERT INTO menu (item_name, price, is_available) VALUES (?, ?, TRUE)";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(insertQuery)) {

            statement.setString(1, name);
            statement.setDouble(2, price);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            logger.warning("error adding menu item " + name + ": " + exception.getMessage());
            return false;
        }
    }

    @Override
    public boolean updatePrice(int menuId, double newPrice) {
        String updateQuery = "UPDATE menu SET price = ? WHERE menu_id = ?";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {

            statement.setDouble(1, newPrice);
            statement.setInt(2, menuId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            logger.warning("error updating price for menuId " + menuId + ": " + exception.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteMenuItem(int menuId) {
        String deleteQuery = "DELETE FROM menu WHERE menu_id = ?";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement statement = connection.prepareStatement(deleteQuery)) {

            statement.setInt(1, menuId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            logger.warning("error deleting menu item " + menuId + ": " + exception.getMessage());
            return false;
        }
    }
}