package com.edusmart.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Centralized DB connection utility for MySQL (XAMPP).
 */
public final class DbConnection {

    private static final String URL = System.getProperty(
            "db.url",
            "jdbc:mysql://localhost:3306/user?useSSL=false&serverTimezone=UTC"
    );
    private static final String USER = System.getProperty("db.user", "root");
    private static final String PASSWORD = System.getProperty("db.password", "");

    private DbConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
