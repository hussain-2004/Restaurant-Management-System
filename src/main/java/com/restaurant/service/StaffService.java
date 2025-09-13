package com.restaurant.service;

import com.restaurant.dao.StaffDAO;
import com.restaurant.model.AbstractStaff;
import com.restaurant.util.LoggerUtil;

import java.util.logging.Logger;

/**
 * staff service is like common helper for any type of staff.
 */
public class StaffService {
    private static final Logger logger = LoggerUtil.grabLogger();
    private final StaffDAO staffDao = new StaffDAO();

    public AbstractStaff findStaffByUser(int userId) {
        return staffDao.getStaffByUserId(userId);
    }
}
