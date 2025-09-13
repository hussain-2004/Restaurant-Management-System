package com.restaurant.dao;

import com.restaurant.config.DatabaseConnection;
import com.restaurant.model.MenuItem;
import com.restaurant.service.interfaces.IMenuService;
import com.restaurant.util.LoggerUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * dao for menu items like pizza, coke or biryani,
 * it can read and also allow admin to update things.
 */
public class MenuDAO implements IMenuService {
    private static final Logger logger = LoggerUtil.grabLogger();

    @Override
    public List<MenuItem> getAllMenuItems() {
        List<MenuItem> result = new ArrayList<>();
        String sql = "SELECT * FROM menu ORDER BY menu_id";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(new MenuItem(rs.getInt("menu_id"),
                        rs.getString("item_name"),
                        rs.getDouble("price"),
                        rs.getBoolean("is_available")));
            }
        } catch (SQLException e) {
            logger.warning("error reading all menu items: " + e.getMessage());
        }
        return result;
    }

    @Override
    public boolean addMenuItem(String name, double price) {
        String sql = "INSERT INTO menu (item_name, price, is_available) VALUES (?, ?, TRUE)";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setDouble(2, price);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.warning("error adding menu item " + name + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updatePrice(int menuId, double newPrice) {
        String sql = "UPDATE menu SET price = ? WHERE menu_id = ?";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, newPrice);
            stmt.setInt(2, menuId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.warning("error updating price for menuId " + menuId + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteMenuItem(int menuId) {
        String sql = "DELETE FROM menu WHERE menu_id = ?";
        try (Connection connection = DatabaseConnection.fetchConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, menuId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.warning("error deleting menu item " + menuId + ": " + e.getMessage());
            return false;
        }
    }
}
