package com.example.smartmusicplayer.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/smart_music_player"; // tên DB
    private static final String USER = "root";  // nếu bạn đổi user, sửa ở đây
    private static final String PASSWORD = "12345678";  // nếu có mật khẩu MySQL thì nhập vào đây

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found!", e);
        }
    }
}
