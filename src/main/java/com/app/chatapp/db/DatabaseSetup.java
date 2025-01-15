package com.app.chatapp.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSetup {
    private final Connection connection;

    public DatabaseSetup(Connection connection) {
        this.connection = connection;
    }
    public void createTables() {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE users (
                    username VARCHAR(50) NOT NULL,
                    password VARBINARY(200) NOT NULL,
                    avatar_path VARCHAR(100) DEFAULT NULL,
                    PRIMARY KEY (username)
                )
            """);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create database tables", e);
        }
    }
}