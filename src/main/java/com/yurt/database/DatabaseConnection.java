package com.yurt.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;
    private final String URL = "jdbc:sqlite:yurt_otomasyon.db";

    private DatabaseConnection() {
        try {
            connection = DriverManager.getConnection(URL);
            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        } else {
            try {
                if (instance.getConnection().isClosed()) {
                    instance = new DatabaseConnection();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private void createTables() {
        String createUsers = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL," +
                "role TEXT NOT NULL)";

        String createRooms = "CREATE TABLE IF NOT EXISTS rooms (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "room_number TEXT UNIQUE NOT NULL," +
                "capacity INTEGER NOT NULL," +
                "current_count INTEGER DEFAULT 0," +
                "type TEXT)";

        String createStudents = "CREATE TABLE IF NOT EXISTS students (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "name TEXT," +
                "surname TEXT," +
                "tc_no TEXT," +
                "phone TEXT," +
                "room_id INTEGER," +
                "FOREIGN KEY(user_id) REFERENCES users(id))";

        String createRequests = "CREATE TABLE IF NOT EXISTS requests (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "student_id INTEGER," +
                "start_date TEXT," +
                "end_date TEXT," +
                "reason TEXT," +
                "status TEXT DEFAULT 'Beklemede'," +
                "FOREIGN KEY(student_id) REFERENCES students(id))";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsers);
            stmt.execute(createRooms);
            stmt.execute(createStudents);
            stmt.execute(createRequests);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}