package com.example.smartmusicplayer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class MusicPlayerApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/smartmusicplayer/views/login.fxml"));
            Scene scene = new Scene(loader.load());

            // Lấy kích thước màn hình
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double screenWidth = screenBounds.getWidth();
            double screenHeight = screenBounds.getHeight();

            // Set kích thước Stage: 70% chiều rộng, full chiều cao
            stage.setWidth(screenWidth * 0.7);
            stage.setHeight(screenHeight);
            stage.centerOnScreen(); // Căn giữa để cách lề trái/phải 15%

            stage.setTitle("Smart Music Player");
            stage.setScene(scene);
            stage.show();
            System.out.println("✅ Ứng dụng khởi động: " + stage.getWidth() + "x" + stage.getHeight());
        } catch (IOException e) {
            System.err.println("❌ Lỗi khi load login.fxml: " + e.getMessage());
            e.printStackTrace();
            throw e; // Ném lỗi để dễ debug
        }
    }

    public static void main(String[] args) {
        launch();
    }
}