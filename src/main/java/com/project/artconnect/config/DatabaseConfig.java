package com.project.artconnect.config;
import java.sql.*;
/**
 * Database configuration constants.
 * TODO: Students should update these with their own MySQL credentials.
 */
public class DatabaseConfig {
    public static final String URL = "jdbc:mysql://localhost:3306/projet_app";
    public static final String USER = "root";
    public static final String PASSWORD = "password"; // CHANGE ME

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("Impossible de se connecter à la base de données", e);
        }
    }
}
