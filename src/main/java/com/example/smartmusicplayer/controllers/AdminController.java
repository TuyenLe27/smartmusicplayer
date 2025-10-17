package com.example.smartmusicplayer.controllers;

import com.example.smartmusicplayer.models.Song;
import com.example.smartmusicplayer.services.SongService;
import com.example.smartmusicplayer.utils.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class AdminController {
    @FXML private VBox adminVBox;
    @FXML private Button manageUsersButton; // Thêm biến này
    @FXML private Button manageFeedbackButton; // Thêm biến này
    @FXML private Button backButton;
    @FXML private Button logoutButton;
    @FXML private TextField songTitleField;
    @FXML private TextField artistField;
    @FXML private TextField albumField;
    @FXML private ComboBox<String> genreComboBox;
    @FXML private TextField songFileField;
    @FXML private TextField coverFileField;
    @FXML private Button browseSongButton;
    @FXML private Button browseCoverButton;
    @FXML private ListView<Song> songList;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private VBox editForm;
    @FXML private TextField editSongTitleField;
    @FXML private TextField editArtistField;
    @FXML private TextField editAlbumField;
    @FXML private ComboBox<String> editGenreComboBox;
    @FXML private TextField editSongFileField;
    @FXML private TextField editCoverFileField;
    @FXML private Button editBrowseSongButton;
    @FXML private Button editBrowseCoverButton;

    private int currentUserId;
    private File selectedSongFile;
    private File selectedCoverFile;
    private File editSelectedSongFile;
    private File editSelectedCoverFile;
    private Song selectedSong;
    private ObservableList<Song> songs;
    private ObservableList<String> genres;
    private MediaPlayer mediaPlayer;

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        refreshSongList();
        refreshGenres();
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }



    @FXML
    public void initialize() {
        songList.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                setupResponsiveBindings((Stage) newScene.getWindow());
            }
        });

        refreshSongList();
        refreshGenres();
        songList.getSelectionModel().selectedItemProperty().addListener((obs, old, newSong) -> {
            if (newSong != null) {
                editButton.setDisable(false);
                deleteButton.setDisable(false); // Bật nút "Xóa" khi chọn bài hát
            } else {
                editButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });
        editButton.setDisable(true);
        deleteButton.setDisable(true);
        System.out.println("✅ Admin Panel initialized");
    }

    @FXML
    private void onManageUsersClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/smartmusicplayer/views/user-management.fxml"));
            Scene scene = new Scene(loader.load());
            UserManagementController controller = loader.getController();
            controller.setCurrentUserId(currentUserId);
            Stage stage = (Stage) manageUsersButton.getScene().getWindow();
            loadScene(stage, scene, "User Management - Smart Music Player");
            System.out.println("✅ Opened User Management");
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Mở trang quản lý người dùng thất bại: " + e.getMessage());
            alert.show();
        }
    }


    public void setupResponsiveBindings(Stage stage) {
        adminVBox.maxWidthProperty().bind(stage.widthProperty().multiply(0.7));
        adminVBox.spacingProperty().bind(stage.heightProperty().multiply(0.02));
        adminVBox.paddingProperty().bindBidirectional(new javafx.beans.property.SimpleObjectProperty<>(
                new Insets(stage.getHeight() * 0.03, stage.getWidth() * 0.15, stage.getHeight() * 0.03, stage.getWidth() * 0.15)));

        songTitleField.prefWidthProperty().bind(adminVBox.widthProperty().multiply(0.8));
        songTitleField.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        artistField.prefWidthProperty().bind(adminVBox.widthProperty().multiply(0.8));
        artistField.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        albumField.prefWidthProperty().bind(adminVBox.widthProperty().multiply(0.8));
        albumField.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        genreComboBox.prefWidthProperty().bind(adminVBox.widthProperty().multiply(0.8));
        genreComboBox.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        songFileField.prefWidthProperty().bind(adminVBox.widthProperty().multiply(0.7));
        songFileField.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        coverFileField.prefWidthProperty().bind(adminVBox.widthProperty().multiply(0.7));
        coverFileField.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        browseSongButton.prefWidthProperty().bind(adminVBox.widthProperty().multiply(0.2));
        browseSongButton.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        browseCoverButton.prefWidthProperty().bind(adminVBox.widthProperty().multiply(0.2));
        browseCoverButton.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        songList.prefWidthProperty().bind(adminVBox.widthProperty().multiply(0.8));
        songList.prefHeightProperty().bind(stage.heightProperty().multiply(0.3));
        VBox.setVgrow(songList, javafx.scene.layout.Priority.ALWAYS);

        editSongTitleField.prefWidthProperty().bind(adminVBox.widthProperty().multiply(0.8));
        editSongTitleField.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        editArtistField.prefWidthProperty().bind(adminVBox.widthProperty().multiply(0.8));
        editArtistField.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        editAlbumField.prefWidthProperty().bind(adminVBox.widthProperty().multiply(0.8));
        editAlbumField.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        editGenreComboBox.prefWidthProperty().bind(adminVBox.widthProperty().multiply(0.8));
        editGenreComboBox.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        editSongFileField.prefWidthProperty().bind(adminVBox.widthProperty().multiply(0.7));
        editSongFileField.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        editCoverFileField.prefWidthProperty().bind(adminVBox.widthProperty().multiply(0.7));
        editCoverFileField.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        editBrowseSongButton.prefWidthProperty().bind(adminVBox.widthProperty().multiply(0.2));
        editBrowseSongButton.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        editBrowseCoverButton.prefWidthProperty().bind(adminVBox.widthProperty().multiply(0.2));
        editBrowseCoverButton.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));

        backButton.prefWidthProperty().bind(adminVBox.widthProperty().multiply(0.3));
        backButton.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        logoutButton.prefWidthProperty().bind(adminVBox.widthProperty().multiply(0.2)); // Giảm từ 0.25 để đủ chỗ
        logoutButton.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        manageUsersButton.prefWidthProperty().bind(adminVBox.widthProperty().multiply(0.2)); // Giảm từ 0.25
        manageUsersButton.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        manageFeedbackButton.prefWidthProperty().bind(adminVBox.widthProperty().multiply(0.2)); // Thêm binding
        manageFeedbackButton.prefHeightProperty().bind(stage.heightProperty().multiply(0.05)); // Thêm binding
        editButton.prefWidthProperty().bind(adminVBox.widthProperty().multiply(0.4));
        editButton.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        deleteButton.prefWidthProperty().bind(adminVBox.widthProperty().multiply(0.4));
        deleteButton.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
    }


    @FXML
    private void onManageFeedbackClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/smartmusicplayer/views/feedback-management.fxml"));
            Scene scene = new Scene(loader.load());
            FeedbackManagementController controller = loader.getController();
            controller.setCurrentUserId(currentUserId);
            Stage stage = (Stage) manageFeedbackButton.getScene().getWindow();
            loadScene(stage, scene, "Feedback Management - Smart Music Player");
            System.out.println("✅ Opened Feedback Management");
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Mở trang quản lý feedback thất bại: " + e.getMessage());
            alert.show();
        }
    }


    private void refreshGenres() {
        SongService service = new SongService();
        genres = FXCollections.observableArrayList(service.getAllGenres());
        genres.add(0, "All Genres"); // Thêm "All Genres" làm tùy chọn mặc định
        genreComboBox.setItems(genres);
        editGenreComboBox.setItems(genres);
        genreComboBox.setValue("All Genres"); // Đặt mặc định
        editGenreComboBox.setValue("All Genres");
        System.out.println("🔄 Refreshed genres: " + genres.size() + " genres");
    }

    private void refreshSongList() {
        SongService service = new SongService();
        List<Song> songData = service.getAllSongs();
        songs = FXCollections.observableArrayList(songData);
        songList.setItems(songs);
        System.out.println("🔄 Refreshed admin song list: " + songs.size() + " songs");
    }

    @FXML
    private void onBrowseSongClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP3 Files", "*.mp3"));
        selectedSongFile = fileChooser.showOpenDialog(backButton.getScene().getWindow());
        if (selectedSongFile != null) {
            String songsDir = "src/main/resources/com/example/smartmusicplayer/songs/";
            String normalizedFileName = normalizeFileName(selectedSongFile.getName());
            String uniqueSongFileName = getUniqueFileName(songsDir, normalizedFileName);
            songFileField.setText(uniqueSongFileName);
            System.out.println("📁 Selected MP3: " + selectedSongFile.getAbsolutePath() + " -> " + uniqueSongFileName);
        }
    }

    @FXML
    private void onBrowseCoverClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png"));
        selectedCoverFile = fileChooser.showOpenDialog(backButton.getScene().getWindow());
        if (selectedCoverFile != null) {
            String coversDir = "src/main/resources/com/example/smartmusicplayer/covers/";
            String normalizedFileName = normalizeFileName(selectedCoverFile.getName());
            String uniqueCoverFileName = getUniqueFileName(coversDir, normalizedFileName);
            coverFileField.setText(uniqueCoverFileName);
            System.out.println("📁 Selected Cover: " + selectedCoverFile.getAbsolutePath() + " -> " + uniqueCoverFileName);
        }
    }

    @FXML
    private void onUploadSongClick() {
        String title = songTitleField.getText().trim();
        String artist = artistField.getText().trim();
        String album = albumField.getText().trim();
        String genre = genreComboBox.getValue();
        String songFileName = songFileField.getText().trim();
        String coverFileName = coverFileField.getText().trim();

        if (title.isEmpty() || songFileName.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Song title and MP3 file are required!");
            alert.show();
            return;
        }

        if ("All Genres".equals(genre)) {
            genre = null; // Lưu NULL nếu chọn "All Genres"
        }

        try {
            String songsDir = "src/main/resources/com/example/smartmusicplayer/songs/";
            String coversDir = "src/main/resources/com/example/smartmusicplayer/covers/";
            Files.createDirectories(Paths.get(songsDir));
            Files.createDirectories(Paths.get(coversDir));

            String normalizedSongFileName = normalizeFileName(selectedSongFile.getName());
            String uniqueSongFileName = getUniqueFileName(songsDir, normalizedSongFileName);
            String songDestPath = songsDir + uniqueSongFileName;
            System.out.println("📁 Copying MP3 to: " + songDestPath);
            Files.copy(selectedSongFile.toPath(), Paths.get(songDestPath), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            File copiedSongFile = new File(songDestPath);
            if (!copiedSongFile.exists()) {
                throw new IOException("Failed to copy MP3 file to: " + songDestPath);
            }
            System.out.println("✅ MP3 copied successfully: " + copiedSongFile.getAbsolutePath());

            String uniqueCoverFileName = null;
            if (selectedCoverFile != null && !coverFileName.isEmpty()) {
                String normalizedCoverFileName = normalizeFileName(selectedCoverFile.getName());
                uniqueCoverFileName = getUniqueFileName(coversDir, normalizedCoverFileName);
                String coverDestPath = coversDir + uniqueCoverFileName;
                System.out.println("📁 Copying cover to: " + coverDestPath);
                Files.copy(selectedCoverFile.toPath(), Paths.get(coverDestPath), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                File copiedCoverFile = new File(coverDestPath);
                if (!copiedCoverFile.exists()) {
                    throw new IOException("Failed to copy cover image to: " + coverDestPath);
                }
                System.out.println("✅ Cover copied successfully: " + copiedCoverFile.getAbsolutePath());
            }

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO songs (title, artist, album, genre, file_path, cover_image, uploaded_by) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                stmt.setString(1, title);
                stmt.setString(2, artist.isEmpty() ? null : artist);
                stmt.setString(3, album.isEmpty() ? null : album);
                stmt.setString(4, genre);
                stmt.setString(5, uniqueSongFileName);
                stmt.setString(6, uniqueCoverFileName);
                stmt.setInt(7, currentUserId);
                System.out.println("💾 Saving to database: title=" + title + ", genre=" + genre + ", file_path=" + uniqueSongFileName + ", cover_image=" + uniqueCoverFileName);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Song uploaded successfully!");
                    alert.show();
                    clearFields();
                    refreshSongList();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to upload song to database!");
                    alert.show();
                }
            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to upload song to database: " + e.getMessage());
                alert.show();
                e.printStackTrace();
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to copy files: " + e.getMessage());
            alert.show();
            e.printStackTrace();
        }
    }

    @FXML
    private void onBrowseEditSongClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP3 Files", "*.mp3"));
        editSelectedSongFile = fileChooser.showOpenDialog(backButton.getScene().getWindow());
        if (editSelectedSongFile != null) {
            String songsDir = "src/main/resources/com/example/smartmusicplayer/songs/";
            String normalizedFileName = normalizeFileName(editSelectedSongFile.getName());
            String uniqueSongFileName = getUniqueFileName(songsDir, normalizedFileName);
            editSongFileField.setText(uniqueSongFileName);
            System.out.println("📁 Selected Edit MP3: " + editSelectedSongFile.getAbsolutePath() + " -> " + uniqueSongFileName);
        }
    }

    @FXML
    private void onBrowseEditCoverClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png"));
        editSelectedCoverFile = fileChooser.showOpenDialog(backButton.getScene().getWindow());
        if (editSelectedCoverFile != null) {
            String coversDir = "src/main/resources/com/example/smartmusicplayer/covers/";
            String normalizedFileName = normalizeFileName(editSelectedCoverFile.getName());
            String uniqueCoverFileName = getUniqueFileName(coversDir, normalizedFileName);
            editCoverFileField.setText(uniqueCoverFileName);
            System.out.println("📁 Selected Edit Cover: " + editSelectedCoverFile.getAbsolutePath() + " -> " + uniqueCoverFileName);
        }
    }

    @FXML
    private void onEditSongClick() {
        selectedSong = songList.getSelectionModel().getSelectedItem();
        if (selectedSong == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a song to edit!");
            alert.show();
            return;
        }

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            System.out.println("⏹️ Stopped MediaPlayer for editing");
        }

        System.out.println("✏️ Editing song: ID=" + selectedSong.getId() + ", Title=" + selectedSong.getTitle() +
                ", Artist=" + selectedSong.getArtist() + ", Album=" + selectedSong.getAlbum() +
                ", Genre=" + selectedSong.getGenre() + ", FilePath=" + selectedSong.getFilePath() +
                ", CoverImage=" + selectedSong.getCoverImage());

        editSongTitleField.setText(selectedSong.getTitle() != null ? selectedSong.getTitle() : "");
        editArtistField.setText(selectedSong.getArtist() != null ? selectedSong.getArtist() : "");
        editAlbumField.setText(selectedSong.getAlbum() != null ? selectedSong.getAlbum() : "");
        editGenreComboBox.setValue(selectedSong.getGenre());
        editSongFileField.setText(selectedSong.getFilePath() != null ? selectedSong.getFilePath() : "");
        editCoverFileField.setText(selectedSong.getCoverImage() != null ? selectedSong.getCoverImage() : "");
        editForm.setVisible(true);
        editForm.setManaged(true);
    }

    @FXML
    private void onSaveEditClick() {
        String title = editSongTitleField.getText().trim();
        String artist = editArtistField.getText().trim();
        String album = editAlbumField.getText().trim();
        String genre = editGenreComboBox.getValue();
        String songFileName = editSongFileField.getText().trim();
        String coverFileName = editCoverFileField.getText().trim();

        if (title.isEmpty() || songFileName.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Song title and MP3 file are required!");
            alert.show();
            return;
        }

        if ("All Genres".equals(genre)) {
            genre = null; // Lưu NULL nếu chọn "All Genres"
        }

        try {
            String songsDir = "src/main/resources/com/example/smartmusicplayer/songs/";
            String coversDir = "src/main/resources/com/example/smartmusicplayer/covers/";
            String newSongFileName = selectedSong.getFilePath();
            String newCoverFileName = selectedSong.getCoverImage();

            if (editSelectedSongFile != null) {
                String normalizedSongFileName = normalizeFileName(editSelectedSongFile.getName());
                newSongFileName = getUniqueFileName(songsDir, normalizedSongFileName);
                String songDestPath = songsDir + newSongFileName;
                System.out.println("📁 Copying new MP3 to: " + songDestPath);
                Files.copy(editSelectedSongFile.toPath(), Paths.get(songDestPath), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                File copiedSongFile = new File(songDestPath);
                if (!copiedSongFile.exists()) {
                    throw new IOException("Failed to copy MP3 file to: " + songDestPath);
                }
                System.out.println("✅ New MP3 copied successfully: " + copiedSongFile.getAbsolutePath());

                if (!selectedSong.getFilePath().equals(newSongFileName)) {
                    Files.deleteIfExists(Paths.get(songsDir + selectedSong.getFilePath()));
                    System.out.println("🗑️ Deleted old MP3: " + selectedSong.getFilePath());
                }
            }

            if (editSelectedCoverFile != null) {
                String normalizedCoverFileName = normalizeFileName(editSelectedCoverFile.getName());
                newCoverFileName = getUniqueFileName(coversDir, normalizedCoverFileName);
                String coverDestPath = coversDir + newCoverFileName;
                System.out.println("📁 Copying new cover to: " + coverDestPath);
                Files.copy(editSelectedCoverFile.toPath(), Paths.get(coverDestPath), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                File copiedCoverFile = new File(coverDestPath);
                if (!copiedCoverFile.exists()) {
                    throw new IOException("Failed to copy cover image to: " + coverDestPath);
                }
                System.out.println("✅ New cover copied successfully: " + copiedCoverFile.getAbsolutePath());

                if (selectedSong.getCoverImage() != null && !selectedSong.getCoverImage().equals(newCoverFileName)) {
                    Files.deleteIfExists(Paths.get(coversDir + selectedSong.getCoverImage()));
                    System.out.println("🗑️ Deleted old cover: " + selectedSong.getCoverImage());
                }
            }

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE songs SET title = ?, artist = ?, album = ?, genre = ?, file_path = ?, cover_image = ? WHERE id = ?")) {
                stmt.setString(1, title);
                stmt.setString(2, artist.isEmpty() ? null : artist);
                stmt.setString(3, album.isEmpty() ? null : album);
                stmt.setString(4, genre);
                stmt.setString(5, newSongFileName);
                stmt.setString(6, newCoverFileName);
                stmt.setInt(7, selectedSong.getId());
                System.out.println("💾 Updating database: title=" + title + ", genre=" + genre + ", file_path=" + newSongFileName + ", cover_image=" + newCoverFileName);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Song updated successfully!");
                    alert.show();
                    editForm.setVisible(false);
                    editForm.setManaged(false);
                    editSelectedSongFile = null;
                    editSelectedCoverFile = null;
                    refreshSongList();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to update song in database: No rows affected");
                    alert.show();
                }
            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to update song in database: " + e.getMessage());
                alert.show();
                e.printStackTrace();
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to copy files: " + e.getMessage());
            alert.show();
            e.printStackTrace();
        }
    }


    @FXML
    private void onCancelEditClick() {
        editForm.setVisible(false);
        editForm.setManaged(false);
        editSelectedSongFile = null;
        editSelectedCoverFile = null;
        System.out.println("🚫 Cancelled editing");
    }

    @FXML
    private void onDeleteSongClick() {
        selectedSong = songList.getSelectionModel().getSelectedItem();
        if (selectedSong == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Vui lòng chọn bài hát để xóa!");
            alert.show();
            return;
        }

        System.out.println("🗑️ Bắt đầu xóa bài hát: ID=" + selectedSong.getId() + ", Title=" + selectedSong.getTitle());

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc muốn xóa bài hát '" + selectedSong.getTitle() + "'?");
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    String songsDir = "src/main/resources/com/example/smartmusicplayer/songs/";
                    String coversDir = "src/main/resources/com/example/smartmusicplayer/covers/";

                    // Dừng MediaPlayer nếu đang phát bài hát này
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        System.out.println("⏹️ Đã dừng MediaPlayer trước khi xóa");
                    }

                    // Xóa bản ghi trong database
                    try (Connection conn = DBConnection.getConnection();
                         PreparedStatement stmt = conn.prepareStatement("DELETE FROM songs WHERE id = ?")) {
                        stmt.setInt(1, selectedSong.getId());
                        int rowsAffected = stmt.executeUpdate();
                        System.out.println("🗑️ Xóa bản ghi database: " + rowsAffected + " dòng bị ảnh hưởng");

                        if (rowsAffected > 0) {
                            // Xóa file MP3
                            String songPath = songsDir + selectedSong.getFilePath();
                            try {
                                Files.deleteIfExists(Paths.get(songPath));
                                System.out.println("🗑️ Đã xóa MP3: " + songPath);
                            } catch (IOException e) {
                                System.out.println("⚠️ Không thể xóa file MP3: " + songPath + ", lỗi: " + e.getMessage());
                            }

                            // Xóa ảnh bìa (nếu có)
                            if (selectedSong.getCoverImage() != null) {
                                String coverPath = coversDir + selectedSong.getCoverImage();
                                try {
                                    Files.deleteIfExists(Paths.get(coverPath));
                                    System.out.println("🗑️ Đã xóa ảnh bìa: " + coverPath);
                                } catch (IOException e) {
                                    System.out.println("⚠️ Không thể xóa ảnh bìa: " + coverPath + ", lỗi: " + e.getMessage());
                                }
                            }

                            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Xóa bài hát thành công!");
                            alert.show();
                            refreshSongList();
                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Xóa bài hát thất bại: Không có dòng nào bị ảnh hưởng");
                            alert.show();
                        }
                    } catch (SQLException e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Xóa bài hát từ database thất bại: " + e.getMessage());
                        alert.show();
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Xóa bài hát thất bại: " + e.getMessage());
                    alert.show();
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void onBackClick() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                System.out.println("⏹️ Đã dừng MediaPlayer trước khi quay lại Music Player");
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/smartmusicplayer/views/music-player.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) backButton.getScene().getWindow();
            loadScene(stage, scene, "Smart Music Player");

            MusicPlayerController controller = loader.getController();
            controller.setCurrentUserId(currentUserId);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Quay lại music player thất bại: " + e.getMessage());
            alert.show();
        }
    }

    @FXML
    private void onLogoutClick() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                System.out.println("⏹️ Đã dừng MediaPlayer trước khi đăng xuất");
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/smartmusicplayer/views/login.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) backButton.getScene().getWindow();
            loadScene(stage, scene, "Smart Music Player");
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Đăng xuất thất bại: " + e.getMessage());
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
    }

    private String normalizeFileName(String fileName) {
        return fileName.replaceAll("\\s+", "_");
    }

    private String getUniqueFileName(String directory, String fileName) {
        File file = new File(directory + fileName);
        if (!file.exists()) {
            return fileName;
        }

        String nameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));
        String extension = fileName.substring(fileName.lastIndexOf('.'));
        int counter = 1;
        String newFileName;
        do {
            newFileName = nameWithoutExt + "_" + counter + extension;
            file = new File(directory + newFileName);
            counter++;
        } while (file.exists());
        return newFileName;
    }

    private void clearFields() {
        songTitleField.clear();
        artistField.clear();
        albumField.clear();
        genreComboBox.setValue("All Genres");
        songFileField.clear();
        coverFileField.clear();
        selectedSongFile = null;
        selectedCoverFile = null;
    }
}