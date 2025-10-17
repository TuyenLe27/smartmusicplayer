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

public class UserManagementController {
    @FXML private VBox userManagementVBox;
    @FXML private ListView<String> userList;
    @FXML private Button addUserButton;
    @FXML private Button editUserButton;
    @FXML private Button deleteUserButton;
    @FXML private Button backButton;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;

    private ObservableList<String> users;
    private int currentUserId;

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    @FXML
    public void initialize() {
        // Khởi tạo danh sách vai trò
        roleComboBox.setItems(FXCollections.observableArrayList("user", "admin"));
        roleComboBox.setPromptText("Chọn vai trò");

        // Khởi tạo danh sách người dùng
        users = FXCollections.observableArrayList();
        userList.setItems(users);

        // Load danh sách người dùng
        refreshUserList();

        // Khi chọn một người dùng, điền thông tin vào form
        userList.getSelectionModel().selectedItemProperty().addListener((obs, old, newValue) -> {
            if (newValue != null) {
                loadUserDetails(newValue);
                editUserButton.setDisable(false);
                deleteUserButton.setDisable(false);
            } else {
                editUserButton.setDisable(true);
                deleteUserButton.setDisable(true);
            }
        });

        // Responsive bindings
        userManagementVBox.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                setupResponsiveBindings((Stage) newScene.getWindow());
            }
        });

        editUserButton.setDisable(true);
        deleteUserButton.setDisable(true);
        System.out.println("✅ User Management initialized");
    }

    private void setupResponsiveBindings(Stage stage) {
        userManagementVBox.prefWidthProperty().bind(stage.widthProperty().multiply(0.7));
        userManagementVBox.spacingProperty().bind(stage.heightProperty().multiply(0.02));
        userManagementVBox.paddingProperty().bindBidirectional(new SimpleObjectProperty<>(
                new Insets(stage.getHeight() * 0.03, stage.getWidth() * 0.15, stage.getHeight() * 0.03, stage.getWidth() * 0.15)));

        userList.prefWidthProperty().bind(userManagementVBox.widthProperty().multiply(0.8));
        userList.prefHeightProperty().bind(stage.heightProperty().multiply(0.4));
        VBox.setVgrow(userList, javafx.scene.layout.Priority.ALWAYS);

        usernameField.prefWidthProperty().bind(userManagementVBox.widthProperty().multiply(0.8));
        usernameField.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        passwordField.prefWidthProperty().bind(userManagementVBox.widthProperty().multiply(0.8));
        passwordField.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        roleComboBox.prefWidthProperty().bind(userManagementVBox.widthProperty().multiply(0.8));
        roleComboBox.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));

        addUserButton.prefWidthProperty().bind(userManagementVBox.widthProperty().multiply(0.25));
        addUserButton.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        editUserButton.prefWidthProperty().bind(userManagementVBox.widthProperty().multiply(0.25));
        editUserButton.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        deleteUserButton.prefWidthProperty().bind(userManagementVBox.widthProperty().multiply(0.25));
        deleteUserButton.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        backButton.prefWidthProperty().bind(userManagementVBox.widthProperty().multiply(0.4));
        backButton.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
    }

    private void refreshUserList() {
        users.clear();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT username FROM users");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(rs.getString("username"));
            }
            System.out.println("📋 Loaded user list: " + users);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách người dùng: " + e.getMessage());
        }
    }

    private void loadUserDetails(String username) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT username, password, role FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                usernameField.setText(rs.getString("username"));
                passwordField.setText(rs.getString("password"));
                roleComboBox.setValue(rs.getString("role"));
                System.out.println("📝 Loaded user details: " + username);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải thông tin người dùng: " + e.getMessage());
        }
    }

    @FXML
    private void onAddUserClick() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng điền đầy đủ thông tin!");
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO users (username, password, role) VALUES (?, ?, ?)")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);
            stmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm người dùng " + username + " thành công!");
            refreshUserList();
            clearForm();
            System.out.println("➕ Added user: " + username);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm người dùng: " + e.getMessage());
        }
    }

    @FXML
    private void onEditUserClick() {
        String selectedUser = userList.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn người dùng để sửa!");
            return;
        }

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng điền đầy đủ thông tin!");
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE users SET username = ?, password = ?, role = ? WHERE username = ?")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);
            stmt.setString(4, selectedUser);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật người dùng " + username + " thành công!");
                refreshUserList();
                clearForm();
                System.out.println("✏️ Updated user: " + username);
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy người dùng!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật người dùng: " + e.getMessage());
        }
    }

    @FXML
    private void onDeleteUserClick() {
        String selectedUser = userList.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn người dùng để xóa!");
            return;
        }

        // Ngăn xóa tài khoản admin với username="admin" và password="123456"
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT username, password FROM users WHERE username = ?")) {
            stmt.setString(1, selectedUser);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getString("username").equals("admin") && rs.getString("password").equals("123456")) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không thể xóa tài khoản Admin chính (admin/123456)!");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể kiểm tra người dùng: " + e.getMessage());
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Xóa người dùng '" + selectedUser + "' và tất cả playlist của họ?");
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DBConnection.getConnection()) {
                    conn.setAutoCommit(false); // Bắt đầu transaction

                    // Lấy user_id
                    int userId = -1;
                    try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM users WHERE username = ?")) {
                        stmt.setString(1, selectedUser);
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                            userId = rs.getInt("id");
                        }
                    }

                    if (userId == -1) {
                        conn.rollback();
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy người dùng!");
                        return;
                    }

                    // Xóa tất cả playlist của user
                    try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM playlists WHERE user_id = ?")) {
                        stmt.setInt(1, userId);
                        stmt.executeUpdate();
                        System.out.println("🗑️ Deleted playlists for user: " + selectedUser);
                    }

                    // Xóa user
                    try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE username = ?")) {
                        stmt.setString(1, selectedUser);
                        int rowsAffected = stmt.executeUpdate();
                        if (rowsAffected > 0) {
                            conn.commit();
                            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xóa người dùng " + selectedUser + " và các playlist thành công!");
                            refreshUserList();
                            clearForm();
                            System.out.println("🗑️ Deleted user: " + selectedUser);
                        } else {
                            conn.rollback();
                            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy người dùng!");
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa người dùng: " + e.getMessage());
                    try (Connection conn = DBConnection.getConnection()) {
                        conn.rollback();
                    } catch (SQLException rollbackEx) {
                        rollbackEx.printStackTrace();
                    }
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
        usernameField.clear();
        passwordField.clear();
        roleComboBox.setValue(null);
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