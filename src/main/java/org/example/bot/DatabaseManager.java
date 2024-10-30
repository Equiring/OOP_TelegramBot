package org.example.bot;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:tasks.db";

    public DatabaseManager() {
        // Конструктор для создания подключения к базе данных
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}