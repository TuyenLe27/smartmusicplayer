package com.example.smartmusicplayer.controllers;

import com.example.smartmusicplayer.utils.DBConnection;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FeedbackManagementController {
    @FXML private VBox feedbackManagementVBox;
    @FXML private ListView<String> feedbackList;
    @FXML private Button addFeedbackButton;
    @FXML private Button editFeedbackButton;
    @FXML private Button deleteFeedbackButton;
    @FXML private Button backButton;
    @FXML private TextField userIdField;
    @FXML private TextField songIdField;
    @FXML private TextField ratingField;
    @FXML private TextArea commentField;

    private ObservableList<String> feedbacks;
    private int currentUserId;

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    @FXML
    public void initialize() {
        feedbacks = FXCollections.observableArrayList();
        feedbackList.setItems(feedbacks);
        refreshFeedbackList();
        feedbackList.getSelectionModel().selectedItemProperty().addListener((obs, old, newValue) -> {
            if (newValue != null) {
                loadFeedbackDetails(newValue);
                editFeedbackButton.setDisable(false);
                deleteFeedbackButton.setDisable(false);
            } else {
                editFeedbackButton.setDisable(true);
                deleteFeedbackButton.setDisable(true);
            }
        });
        feedbackManagementVBox.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                setupResponsiveBindings((Stage) newScene.getWindow());
            }
        });
        editFeedbackButton.setDisable(true);
        deleteFeedbackButton.setDisable(true);
        System.out.println("✅ Feedback Management initialized");
    }

    private void setupResponsiveBindings(Stage stage) {
        feedbackManagementVBox.prefWidthProperty().bind(stage.widthProperty().multiply(0.7));
        feedbackManagementVBox.spacingProperty().bind(stage.heightProperty().multiply(0.02));
        feedbackManagementVBox.paddingProperty().bindBidirectional(new SimpleObjectProperty<>(
                new Insets(stage.getHeight() * 0.03, stage.getWidth() * 0.15, stage.getHeight() * 0.03, stage.getWidth() * 0.15)));
        feedbackList.prefWidthProperty().bind(feedbackManagementVBox.widthProperty().multiply(0.8));
        feedbackList.prefHeightProperty().bind(stage.heightProperty().multiply(0.4));
        VBox.setVgrow(feedbackList, javafx.scene.layout.Priority.ALWAYS);
        userIdField.prefWidthProperty().bind(feedbackManagementVBox.widthProperty().multiply(0.8));
        userIdField.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        songIdField.prefWidthProperty().bind(feedbackManagementVBox.widthProperty().multiply(0.8));
        songIdField.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        ratingField.prefWidthProperty().bind(feedbackManagementVBox.widthProperty().multiply(0.8));
        ratingField.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        commentField.prefWidthProperty().bind(feedbackManagementVBox.widthProperty().multiply(0.8));
        commentField.prefHeightProperty().bind(stage.heightProperty().multiply(0.1));
        addFeedbackButton.prefWidthProperty().bind(feedbackManagementVBox.widthProperty().multiply(0.25));
        addFeedbackButton.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        editFeedbackButton.prefWidthProperty().bind(feedbackManagementVBox.widthProperty().multiply(0.25));
        editFeedbackButton.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        deleteFeedbackButton.prefWidthProperty().bind(feedbackManagementVBox.widthProperty().multiply(0.25));
        deleteFeedbackButton.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        backButton.prefWidthProperty().bind(feedbackManagementVBox.widthProperty().multiply(0.4));
        backButton.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
    }

    private void refreshFeedbackList() {
        feedbacks.clear();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT f.id, u.username, s.title, f.rating " +
                             "FROM feedback f " +
                             "JOIN users u ON f.user_id = u.id " +
                             "JOIN songs s ON f.song_id = s.id");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                feedbacks.add("ID: " + rs.getInt("id") + ", User: " + rs.getString("username") +
                        ", Song: " + rs.getString("title") + ", Rating: " + rs.getInt("rating"));
            }
            System.out.println("📋 Loaded feedback list: " + feedbacks);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách feedback: " + e.getMessage());
        }
    }

    private void loadFeedbackDetails(String feedbackString) {
        try {
            int feedbackId = Integer.parseInt(feedbackString.split(",")[0].replace("ID: ", "").trim());
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT f.user_id, f.song_id, f.rating, f.comment " +
                                 "FROM feedback f WHERE f.id = ?")) {
                stmt.setInt(1, feedbackId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    userIdField.setText(String.valueOf(rs.getInt("user_id")));
                    songIdField.setText(String.valueOf(rs.getInt("song_id")));
                    ratingField.setText(String.valueOf(rs.getInt("rating")));
                    commentField.setText(rs.getString("comment") != null ? rs.getString("comment") : "");
                    System.out.println("📝 Loaded feedback details: " + feedbackId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải thông tin feedback: " + e.getMessage());
        }
    }

    @FXML
    private void onAddFeedbackClick() {
        String userId = userIdField.getText().trim();
        String songId = songIdField.getText().trim();
        String rating = ratingField.getText().trim();
        String comment = commentField.getText().trim();

        if (userId.isEmpty() || songId.isEmpty() || rating.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng điền đầy đủ User ID, Song ID, và Rating!");
            return;
        }

        try {
            int userIdInt = Integer.parseInt(userId);
            int songIdInt = Integer.parseInt(songId);
            int ratingInt = Integer.parseInt(rating);

            if (ratingInt < 1 || ratingInt > 5) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Rating phải từ 1 đến 5!");
                return;
            }

            // Kiểm tra user_id và song_id tồn tại
            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement checkUser = conn.prepareStatement("SELECT id FROM users WHERE id = ?");
                checkUser.setInt(1, userIdInt);
                if (!checkUser.executeQuery().next()) {
                    showAlert(Alert.AlertType.WARNING, "Cảnh báo", "User ID không tồn tại!");
                    return;
                }
                PreparedStatement checkSong = conn.prepareStatement("SELECT id FROM songs WHERE id = ?");
                checkSong.setInt(1, songIdInt);
                if (!checkSong.executeQuery().next()) {
                    showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Song ID không tồn tại!");
                    return;
                }
            }

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO feedback (user_id, song_id, rating, comment) VALUES (?, ?, ?, ?)")) {
                stmt.setInt(1, userIdInt);
                stmt.setInt(2, songIdInt);
                stmt.setInt(3, ratingInt);
                stmt.setString(4, comment.isEmpty() ? null : comment);
                stmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm feedback thành công!");
                refreshFeedbackList();
                clearForm();
                System.out.println("➕ Added feedback: User " + userId + ", Song " + songId);
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "User ID, Song ID, và Rating phải là số nguyên!");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm feedback: " + e.getMessage());
        }
    }

    @FXML
    private void onEditFeedbackClick() {
        String selectedFeedback = feedbackList.getSelectionModel().getSelectedItem();
        if (selectedFeedback == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn feedback để sửa!");
            return;
        }

        String userId = userIdField.getText().trim();
        String songId = songIdField.getText().trim();
        String rating = ratingField.getText().trim();
        String comment = commentField.getText().trim();

        if (userId.isEmpty() || songId.isEmpty() || rating.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng điền đầy đủ User ID, Song ID, và Rating!");
            return;
        }

        try {
            int feedbackId = Integer.parseInt(selectedFeedback.split(",")[0].replace("ID: ", "").trim());
            int userIdInt = Integer.parseInt(userId);
            int songIdInt = Integer.parseInt(songId);
            int ratingInt = Integer.parseInt(rating);

            if (ratingInt < 1 || ratingInt > 5) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Rating phải từ 1 đến 5!");
                return;
            }

            // Kiểm tra user_id và song_id tồn tại
            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement checkUser = conn.prepareStatement("SELECT id FROM users WHERE id = ?");
                checkUser.setInt(1, userIdInt);
                if (!checkUser.executeQuery().next()) {
                    showAlert(Alert.AlertType.WARNING, "Cảnh báo", "User ID không tồn tại!");
                    return;
                }
                PreparedStatement checkSong = conn.prepareStatement("SELECT id FROM songs WHERE id = ?");
                checkSong.setInt(1, songIdInt);
                if (!checkSong.executeQuery().next()) {
                    showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Song ID không tồn tại!");
                    return;
                }
            }

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE feedback SET user_id = ?, song_id = ?, rating = ?, comment = ? WHERE id = ?")) {
                stmt.setInt(1, userIdInt);
                stmt.setInt(2, songIdInt);
                stmt.setInt(3, ratingInt);
                stmt.setString(4, comment.isEmpty() ? null : comment);
                stmt.setInt(5, feedbackId);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật feedback thành công!");
                    refreshFeedbackList();
                    clearForm();
                    System.out.println("✏️ Updated feedback: ID " + feedbackId);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy feedback!");
                }
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "User ID, Song ID, và Rating phải là số nguyên!");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật feedback: " + e.getMessage());
        }
    }

    @FXML
    private void onDeleteFeedbackClick() {
        String selectedFeedback = feedbackList.getSelectionModel().getSelectedItem();
        if (selectedFeedback == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn feedback để xóa!");
            return;
        }

        int feedbackId = Integer.parseInt(selectedFeedback.split(",")[0].replace("ID: ", "").trim());

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Xóa feedback ID " + feedbackId + "?");
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("DELETE FROM feedback WHERE id = ?")) {
                    stmt.setInt(1, feedbackId);
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xóa feedback ID " + feedbackId + " thành công!");
                        refreshFeedbackList();
                        clearForm();
                        System.out.println("🗑️ Deleted feedback: ID " + feedbackId);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy feedback!");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa feedback: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void onBackClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/smartmusicplayer/views/admin-panel.fxml"));
            Scene scene = new Scene(loader.load());
            AdminController controller = loader.getController();
            controller.setCurrentUserId(currentUserId);
            Stage stage = (Stage) backButton.getScene().getWindow();
            loadScene(stage, scene, "Admin Panel - Smart Music Player");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Quay lại Admin Panel thất bại: " + e.getMessage());
        }
    }

    private void clearForm() {
        userIdField.clear();
        songIdField.clear();
        ratingField.clear();
        commentField.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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
    }
}