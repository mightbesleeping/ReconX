package com.reconx;

import java.sql.*;

public class DatabaseManager {
    // This creates a file named 'reconx.db' in your project folder
    private static final String URL = "jdbc:sqlite:reconx.db";

    public static void initialize() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite Driver not found: " + e.getMessage());
        }

        // Create the table if it doesn't exist
        String sql = "CREATE TABLE IF NOT EXISTS history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "target TEXT NOT NULL," +
                "type TEXT NOT NULL," +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ");";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("DB Init Error: " + e.getMessage());
        }
    }

    public static void saveSearch(String target, String type) {
        String sql = "INSERT INTO history(target, type) VALUES(?, ?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, target);
            pstmt.setString(2, type);
            pstmt.executeUpdate();
            System.out.println("[DB] Saved: " + target);
        } catch (SQLException e) {
            System.out.println("DB Save Error: " + e.getMessage());
        }
    }

    public static String getHistory() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- RECONX SEARCH HISTORY ---\n");
        String sql = "SELECT * FROM history ORDER BY timestamp DESC LIMIT 20";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                sb.append(String.format("[%s] %s (%s)\n",
                        rs.getString("timestamp"),
                        rs.getString("target"),
                        rs.getString("type")));
            }
        } catch (SQLException e) {
            return "[!] DB Fetch Error: " + e.getMessage();
        }
        return sb.toString();
    }
}