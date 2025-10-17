package com.example.smartmusicplayer.controllers;

import com.example.smartmusicplayer.utils.DBConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegisterController {
    @FXML private VBox registerVBox;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField fullnameField;
    @FXML private Button registerButton;
    @FXML private Button backButton;

    @FXML
    private void initialize() {
        // Để trống để tránh NullPointerException
    }

    public void setupResponsiveBindings(Stage stage) {
        registerVBox.maxWidthProperty().bind(stage.widthProperty().multiply(0.8));
        usernameField.maxWidthProperty().bind(registerVBox.widthProperty().multiply(0.8));
        passwordField.maxWidthProperty().bind(registerVBox.widthProperty().multiply(0.8));
        fullnameField.maxWidthProperty().bind(registerVBox.widthProperty().multiply(0.8));
        registerButton.maxWidthProperty().bind(registerVBox.widthProperty().multiply(0.5));
        backButton.maxWidthProperty().bind(registerVBox.widthProperty().multiply(0.5));
    }

    @FXML
    private void onRegisterClick() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String fullname = fullnameField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Username and password are required!");
            alert.show();
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?")) {
                checkStmt.setString(1, username);
                var result = checkStmt.executeQuery();
                if (result.next() && result.getInt(1) > 0) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Username already exists!");
                    alert.show();
                    return;
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO users (username, password, fullname, role) VALUES (?, ?, ?, 'user')")) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.setString(3, fullname);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Registration successful!");
                    alert.show();
                    onBackClick();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Registration failed: No rows affected!");
                    alert.show();
                }
            }
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Registration failed: " + e.getMessage());
            alert.show();
            e.printStackTrace();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Unexpected error: " + e.getMessage());
            alert.show();
            e.printStackTrace();
        }
    }

    @FXML
    private void onBackClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/smartmusicplayer/views/login.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) usernameField.getScene().getWindow();
            loadScene(stage, scene, "Smart Music Player");

            LoginController controller = loader.getController();
            controller.setupResponsiveBindings(stage);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Mở màn hình đăng nhập thất bại: " + e.getMessage());
            alert.show();
        }
    }

    private void loadScene(Stage stage, Scene scene, String title) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();

        stage.setWidth(screenWidth * 0.7);
        stage.setHeight(screenHeight);
        stage.centerOnScreen();

        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
        System.out.println("✅ Chuyển cảnh: " + title + " (" + stage.getWidth() + "x" + stage.getHeight() + ")");

        if (title.equals("Đăng Ký - Smart Music Player")) {
            setupResponsiveBindings(stage);
        }
    }
}