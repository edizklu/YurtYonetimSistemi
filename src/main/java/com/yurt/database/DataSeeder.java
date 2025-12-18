package com.yurt.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DataSeeder {
    public static void seedData() {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (!userExists(conn, "admin")) {
            addSimpleUser(conn, "admin", "1234", "STAFF");
            System.out.println("Test Verisi: admin/1234 eklendi.");
        }
        if (!userExists(conn, "ogrenci1")) {
            addSimpleUser(conn, "ogrenci1", "1234", "STUDENT");
            System.out.println("Test Verisi: ogrenci1/1234 eklendi.");
        }
    }

    private static boolean userExists(Connection conn, String username) {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT id FROM users WHERE username = ?");
            stmt.setString(1, username);
            return stmt.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    private static void addSimpleUser(Connection conn, String u, String p, String r) {
        try {
            String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, u); stmt.setString(2, p); stmt.setString(3, r);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}