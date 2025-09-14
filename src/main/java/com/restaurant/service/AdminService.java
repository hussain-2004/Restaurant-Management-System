package com.restaurant.service;

import com.restaurant.dao.MenuDAO;
import com.restaurant.dao.StaffDAO;
import com.restaurant.model.AbstractStaff;
import com.restaurant.model.MenuItem;
import com.restaurant.util.LoggerUtil;

import java.util.List;
import java.util.logging.Logger;

/**
 * admin service is super control.
 */
public class AdminService {
    private static final Logger logger = LoggerUtil.grabLogger();

    private final MenuDAO menuDataAccessObject = new MenuDAO();
    private final StaffDAO staffDataAccessObject = new StaffDAO();

    public List<MenuItem> seeAllMenuItems() {
        return menuDataAccessObject.getAllMenuItems();
    }

    public boolean addMenuDish(String name, double price) {
        return   menuDataAccessObject.addMenuItem(name, price);
    }

    public boolean changeDishPrice(int menuId, double price) {
        return  menuDataAccessObject.updatePrice(menuId, price);
    }

    public boolean deleteDish(int menuId) {
        return menuDataAccessObject.deleteMenuItem(menuId);
    }

    public AbstractStaff getStaffByUser(int userId) {
        return staffDataAccessObject.getStaffByUserId(userId);
    }
}