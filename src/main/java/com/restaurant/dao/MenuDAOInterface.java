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
 * dao for menu items like pizza, coke or biryani,
 * it can read and also allow admin to update things.
 */
public class MenuDAOInterface implements MenuServiceInterface {
    private static final Logger logger = LoggerUtil.grabLogger();

    @Override
    public List<MenuItem> getAllMenuItems() {
        List<MenuItem> allMenuItemsList = new ArrayList<>();
        String selectAllMenuItemsQuery = "SELECT * FROM menu ORDER BY menu_id";
        try (Connection databaseConnection = DatabaseConnection.fetchConnection();
             PreparedStatement selectAllMenuItemsStatement = databaseConnection.prepareStatement(selectAllMenuItemsQuery);
             ResultSet menuItemsResultSet = selectAllMenuItemsStatement.executeQuery()) {

            while (menuItemsResultSet.next()) {
                allMenuItemsList.add(new MenuItem(menuItemsResultSet.getInt("menu_id"),
                        menuItemsResultSet.getString("item_name"),
                        menuItemsResultSet.getDouble("price"),
                        menuItemsResultSet.getBoolean("is_available")));
            }
        } catch (SQLException sqlException) {
            logger.warning("error reading all menu items: " + sqlException.getMessage());
        }
        return allMenuItemsList;
    }

    @Override
    public boolean addMenuItem(String name, double price) {
        String insertNewMenuItemQuery = "INSERT INTO menu (item_name, price, is_available) VALUES (?, ?, TRUE)";
        try (Connection databaseConnection = DatabaseConnection.fetchConnection();
             PreparedStatement insertMenuItemStatement = databaseConnection.prepareStatement(insertNewMenuItemQuery)) {

            insertMenuItemStatement.setString(1, name);
            insertMenuItemStatement.setDouble(2, price);
            return insertMenuItemStatement.executeUpdate() > 0;
        } catch (SQLException sqlException) {
            logger.warning("error adding menu item " + name + ": " + sqlException.getMessage());
            return false;
        }
    }

    @Override
    public boolean updatePrice(int menuId, double newPrice) {
        String updateMenuItemPriceQuery = "UPDATE menu SET price = ? WHERE menu_id = ?";
        try (Connection databaseConnection = DatabaseConnection.fetchConnection();
             PreparedStatement updateMenuPriceStatement = databaseConnection.prepareStatement(updateMenuItemPriceQuery)) {

            updateMenuPriceStatement.setDouble(1, newPrice);
            updateMenuPriceStatement.setInt(2, menuId);
            return updateMenuPriceStatement.executeUpdate() > 0;
        } catch (SQLException sqlException) {
            logger.warning("error updating price for menuId " + menuId + ": " + sqlException.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteMenuItem(int menuId) {
        String deleteMenuItemByIdQuery = "DELETE FROM menu WHERE menu_id = ?";
        try (Connection databaseConnection = DatabaseConnection.fetchConnection();
             PreparedStatement deleteMenuItemStatement = databaseConnection.prepareStatement(deleteMenuItemByIdQuery)) {

            deleteMenuItemStatement.setInt(1, menuId);
            return deleteMenuItemStatement.executeUpdate() > 0;
        } catch (SQLException sqlException) {
            logger.warning("error deleting menu item " + menuId + ": " + sqlException.getMessage());
            return false;
        }
    }
}