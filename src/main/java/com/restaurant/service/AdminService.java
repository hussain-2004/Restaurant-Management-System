package com.restaurant.service;

import com.restaurant.dao.MenuDAOInterface;
import com.restaurant.dao.StaffDAO;
import com.restaurant.model.AbstractStaff;
import com.restaurant.model.MenuItem;
import com.restaurant.util.LoggerUtil;

import java.util.List;
import java.util.logging.Logger;

/**
 * admin service is super control, can handle menu and staff list and sales reports.
 */
public class AdminService {
    private static final Logger logger = LoggerUtil.grabLogger();

    private final MenuDAOInterface menuDao = new MenuDAOInterface();
    private final StaffDAO staffDao = new StaffDAO();

    public List<MenuItem> seeAllMenuItems() {
        return menuDao.getAllMenuItems();
    }

    public boolean addMenuDish(String name, double price) {
        return menuDao.addMenuItem(name, price);
    }

    public boolean changeDishPrice(int menuId, double price) {
        return menuDao.updatePrice(menuId, price);
    }

    public boolean deleteDish(int menuId) {
        return menuDao.deleteMenuItem(menuId);
    }

    public AbstractStaff getStaffByUser(int userId) {
        return staffDao.getStaffByUserId(userId);
    }
}
