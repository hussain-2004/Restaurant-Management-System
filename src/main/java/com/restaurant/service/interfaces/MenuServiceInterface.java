package com.restaurant.service.interfaces;

import com.restaurant.model.MenuItem;

import java.util.List;

public interface MenuServiceInterface {
    List<MenuItem> getAllMenuItems();

    boolean addMenuItem(String name, double price);

    boolean updatePrice(int menuId, double newPrice);

    boolean deleteMenuItem(int menuId);
}
