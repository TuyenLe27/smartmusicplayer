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

public class LoginController {
    @FXML private VBox loginVBox;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;

    @FXML
    private void initialize() {
        // Để trống để tránh NullPointerException
        // Bindings sẽ được thiết lập sau khi Scene được gắn
    }

    public void setupResponsiveBindings(Stage stage) {
        loginVBox.maxWidthProperty().bind(stage.widthProperty().multiply(0.8)); // 80% chiều rộng Stage
        usernameField.maxWidthProperty().bind(loginVBox.widthProperty().multiply(0.8)); // 80% VBox
        passwordField.maxWidthProperty().bind(loginVBox.widthProperty().multiply(0.8)); // 80% VBox
        loginButton.maxWidthProperty().bind(loginVBox.widthProperty().multiply(0.5)); // 50% VBox
        registerButton.maxWidthProperty().bind(loginVBox.widthProperty().multiply(0.5)); // 50% VBox
    }

    @FXML
    private void onLoginClick() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Username and password are required!");
            alert.show();
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id, role FROM users WHERE username = ? AND password = ?")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("id");
                String role = rs.getString("role");

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/smartmusicplayer/views/music-player.fxml"));
                Scene scene = new Scene(loader.load());

                MusicPlayerController controller = loader.getController();
                controller.setCurrentUserId(userId);

                Stage stage = (Stage) usernameField.getScene().getWindow();
                loadScene(stage, scene, "Smart Music Player");
                System.out.println("✅ Đăng nhập thành công (" + role + "): " + username);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid credentials!");
                alert.show();
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Login failed: " + e.getMessage());
            alert.show();
            e.printStackTrace();
        }
    }

    @FXML
    private void onRegisterClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/smartmusicplayer/views/register.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) usernameField.getScene().getWindow();
            loadScene(stage, scene, "Đăng Ký - Smart Music Player");

            RegisterController controller = loader.getController();
            controller.setupResponsiveBindings(stage);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Mở màn hình đăng ký thất bại: " + e.getMessage());
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

        if (title.equals("Smart Music Player") && this instanceof LoginController) {
            setupResponsiveBindings(stage);
        }
    }
}