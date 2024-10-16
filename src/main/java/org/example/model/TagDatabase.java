package org.example.model;

import java.awt.event.WindowEvent;
import java.sql.*;

public class TagDatabase {
    private Connection connection;

    public TagDatabase(String dbUrl) {
        try {
            connection = DriverManager.getConnection(dbUrl);
            initializeDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Инициализация базы данных
    private void initializeDatabase() {
        try {
            Statement stmt = connection.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS tags (tag TEXT PRIMARY KEY, placeholder TEXT)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Получение подсказки из базы данных
    public String getPlaceholder(String tag) {
        String placeholder = null;
        try {
            PreparedStatement pstmt = connection.prepareStatement("SELECT placeholder FROM tags WHERE tag = ?");
            pstmt.setString(1, tag);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                placeholder = rs.getString("placeholder");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return placeholder;
    }

    // Сохранение нового тега в базу данных
    public void saveTag(String tag) {
        try {
            PreparedStatement pstmt = connection.prepareStatement("INSERT OR IGNORE INTO tags (tag) VALUES (?)");
            pstmt.setString(1, tag);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Закрытие соединения с базой данных
    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
