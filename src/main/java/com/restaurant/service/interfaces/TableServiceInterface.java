package com.restaurant.service.interfaces;

import com.restaurant.model.Table;

import java.util.List;

public interface TableServiceInterface {

    Table getAvailableTable(int requiredSeats);

    boolean assignTable(int tableId);

    boolean freeTable(int tableId);

    List<Table> getAllTables();

    List<Table> getVacantTables();
}
