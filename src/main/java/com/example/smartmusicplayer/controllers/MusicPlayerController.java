package com.example.smartmusicplayer.controllers;

import com.example.smartmusicplayer.models.Feedback;
import com.example.smartmusicplayer.models.Playlist;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Point2D;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.util.Random; // Thêm import
// Thêm import
import javafx.stage.Popup;
import javafx.util.Duration;
import java.util.function.Function;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MusicPlayerController {
    // Trong MusicPlayerController.java, thêm vào đầu class
    @FXML private Label songDurationLabel; // Label hiển thị thời lượng
    @FXML private ListView<Song> songList;
    @FXML private ImageView coverImage;
    @FXML private Label songTitle;
    @FXML private Label songArtist;
    @FXML private Slider progressBar;
    @FXML private VBox centerVBox;
    @FXML private HBox controlBox;
    @FXML private ComboBox<Playlist> playlistComboBox;
    @FXML private Button createPlaylistButton;
    @FXML private Button addSongToPlaylistButton;
    @FXML private Button removeSongFromPlaylistButton;
    @FXML private ListView<Feedback> feedbackList;
    @FXML private Button addFeedbackButton;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> genreComboBox;
    @FXML private Button searchButton;
    @FXML private Button clearSearchButton;
    @FXML private Button playButton;
    @FXML private Button pauseButton;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Button shuffleButton; // Thêm biến
    @FXML private Button repeatButton; // Thêm biến

    private ObservableList<Song> songs;
    private ObservableList<Playlist> playlists;
    private ObservableList<Feedback> feedbacks;
    private ObservableList<String> genres;
    private MediaPlayer mediaPlayer;
    private int currentIndex = 0;
    private int currentUserId;
    private SongService songService;
    private int repeatMode = 0; // 0: Tắt, 1: Lặp một bài, 2: Lặp playlist

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        songService = new SongService();
        initializeAdminFeatures();
        refreshPlaylists();
        refreshGenres();
        refreshSongList();
    }

    public class AutoCompleteTextField {
        private TextField textField;
        private Popup popup;
        private ListView<String> suggestionList;

        public AutoCompleteTextField(TextField textField, Function<String, List<String>> suggestionProvider) {
            this.textField = textField;
            this.popup = new Popup();
            this.suggestionList = new ListView<>();
            suggestionList.setPrefWidth(textField.getPrefWidth());
            suggestionList.setPrefHeight(150);
            suggestionList.setStyle("-fx-font-size: 14px;");
            popup.getContent().add(suggestionList);

            textField.textProperty().addListener((obs, old, newText) -> {
                if (newText != null && !newText.isEmpty()) {
                    List<String> suggestions = suggestionProvider.apply(newText);
                    if (!suggestions.isEmpty()) {
                        suggestionList.setItems(FXCollections.observableArrayList(suggestions));
                        if (!popup.isShowing()) {
                            // Tính tọa độ chính xác bằng localToScreen
                            Point2D point = textField.localToScreen(0, textField.getHeight());
                            double x = point.getX();
                            double y = point.getY() + 10; // Tăng offset lên 10px
                            System.out.println("🔍 Popup position: x=" + x + ", y=" + y + ", textField height=" + textField.getHeight());
                            popup.show(textField, x, y);
                        }
                    } else {
                        popup.hide();
                    }
                } else {
                    popup.hide();
                }
            });

            suggestionList.setOnMouseClicked(e -> {
                String selected = suggestionList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    textField.setText(selected);
                    popup.hide();
                    String genre = genreComboBox.getSelectionModel().getSelectedItem();
                    songs = FXCollections.observableArrayList(songService.searchSongs(selected, genre));
                    songList.setItems(songs);
                    System.out.println("🔍 Autocomplete selected: " + selected + ", found " + songs.size() + " songs");
                }
            });

            textField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (!isFocused && !suggestionList.isFocused()) {
                    popup.hide();
                    System.out.println("🔍 Popup hidden due to lost focus");
                }
            });

            textField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    popup.hide();
                    System.out.println("🔍 Popup hidden due to Enter key");
                }
            });
        }
    }

    @FXML
    public void initialize() {
        songService = new SongService();
        refreshPlaylists();
        refreshGenres();
        refreshSongList();
        if (!songs.isEmpty()) {
            songList.getSelectionModel().select(0);
            loadSong(songs.get(0));
        }

        songList.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                setupResponsiveBindings((Stage) newScene.getWindow());
                newScene.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.SPACE) {
                        if (mediaPlayer != null) {
                            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                                onPauseClick();
                            } else {
                                onPlayClick();
                            }
                        }
                        event.consume();
                    } else if (event.getCode() == KeyCode.RIGHT) {
                        onNextClick();
                        event.consume();
                    } else if (event.getCode() == KeyCode.LEFT) {
                        onPrevClick();
                        event.consume();
                    } else if (event.getCode() == KeyCode.S) {
                        onShuffleClick();
                        event.consume();
                    } else if (event.getCode() == KeyCode.R) {
                        onRepeatClick();
                        event.consume();
                    } else if (event.getCode() == KeyCode.M) {
                        if (mediaPlayer != null) {
                            mediaPlayer.setMute(!mediaPlayer.isMute());
                            System.out.println("🔇 Mute: " + mediaPlayer.isMute());
                        }
                        event.consume();
                    } else if (event.getCode() == KeyCode.F) {
                        if (mediaPlayer != null) {
                            Duration currentTime = mediaPlayer.getCurrentTime();
                            Duration newTime = currentTime.add(Duration.seconds(10));
                            mediaPlayer.seek(newTime);
                            System.out.println("⏩ Fast forward 10s to: " + newTime.toSeconds() + " seconds");
                        }
                        event.consume();
                    } else if (event.getCode() == KeyCode.B) {
                        if (mediaPlayer != null) {
                            Duration currentTime = mediaPlayer.getCurrentTime();
                            Duration newTime = currentTime.subtract(Duration.seconds(10));
                            if (newTime.toSeconds() < 0) newTime = Duration.ZERO;
                            mediaPlayer.seek(newTime);
                            System.out.println("⏪ Rewind 10s to: " + newTime.toSeconds() + " seconds");
                        }
                        event.consume();
                    }
                    System.out.println("🔐 Key pressed: " + event.getCode());
                });
            }
        });

        songList.setCellFactory(lv -> {
            ListCell<Song> cell = new ListCell<>() {
                private final Label titleLabel = new Label();
                private final Label artistLabel = new Label();
                private final HBox hbox = new HBox(titleLabel, artistLabel);
                @Override
                protected void updateItem(Song song, boolean empty) {
                    super.updateItem(song, empty);
                    if (empty || song == null) {
                        setGraphic(null);
                        setStyle(null);
                    } else {
                        String artist = song.getArtist() != null ? song.getArtist() : "Unknown";
                        String displayText = song.getTitle() + " - " + artist;
                        titleLabel.setText(song.getTitle());
                        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 5px;");
                        artistLabel.setText(" - " + artist);
                        artistLabel.setStyle("-fx-font-size: 14px; -fx-padding: 5px;");
                        hbox.setSpacing(0);
                        setGraphic(hbox);
                        setStyle("-fx-alignment: center-left;");
                        setPrefHeight(60);
                        System.out.println("🔍 Displaying song: " + displayText + ", cell height: " + getHeight());
                    }
                }
            };
            cell.heightProperty().addListener((obs, old, newHeight) -> {
                System.out.println("🔍 ListCell height: " + newHeight);
            });
            songList.getStylesheets().clear();
            songList.setStyle("-fx-control-inner-background: white; -fx-font-size: 14px; -fx-cell-size: 60; -fx-wrap-text: false;");
            songList.setFixedCellSize(60);
            cell.setOnDragDetected(event -> {
                if (!cell.isEmpty()) {
                    Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(String.valueOf(cell.getIndex()));
                    db.setContent(content);
                    event.consume();
                }
            });
            cell.setOnDragOver(event -> {
                if (event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });
            cell.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasString()) {
                    int draggedIndex = Integer.parseInt(db.getString());
                    int dropIndex = cell.getIndex();
                    if (draggedIndex != dropIndex) {
                        Song draggedSong = songs.get(draggedIndex);
                        songs.remove(draggedIndex);
                        songs.add(dropIndex, draggedSong);
                        Playlist selectedPlaylist = playlistComboBox.getSelectionModel().getSelectedItem();
                        if (selectedPlaylist != null && selectedPlaylist.getId() != 0) {
                            for (int i = 0; i < songs.size(); i++) {
                                songService.updateSongOrderInPlaylist(selectedPlaylist.getId(), songs.get(i).getId(), i);
                            }
                            refreshSongList(selectedPlaylist.getId());
                        }
                        songList.getSelectionModel().select(dropIndex);
                        System.out.println("🚀 Dragged from index: " + draggedIndex + " to: " + dropIndex);
                    }
                    event.setDropCompleted(true);
                    event.consume();
                }
            });
            cell.setOnDragDone(DragEvent::consume);
            return cell;
        });
        songList.getStylesheets().clear();

        songList.getSelectionModel().selectedItemProperty().addListener((obs, old, newSong) -> {
            if (newSong != null) {
                loadSong(newSong);
                addSongToPlaylistButton.setDisable(false);
                addFeedbackButton.setDisable(false);
                removeSongFromPlaylistButton.setDisable(playlistComboBox.getSelectionModel().getSelectedItem() == null);
                refreshFeedbackList(newSong.getId());
            } else {
                addSongToPlaylistButton.setDisable(true);
                addFeedbackButton.setDisable(true);
                removeSongFromPlaylistButton.setDisable(true);
                feedbackList.setItems(null);
            }
        });

        playlistComboBox.getSelectionModel().selectedItemProperty().addListener((obs, old, newPlaylist) -> {
            if (newPlaylist != null) {
                if (newPlaylist.getId() == 0) {
                    refreshSongList();
                } else {
                    refreshSongList(newPlaylist.getId());
                }
                removeSongFromPlaylistButton.setDisable(newPlaylist.getId() == 0);
            } else {
                refreshSongList();
                removeSongFromPlaylistButton.setDisable(true);
            }
        });

        feedbackList.setCellFactory(lv -> new ListCell<Feedback>() {
            @Override
            protected void updateItem(Feedback feedback, boolean empty) {
                super.updateItem(feedback, empty);
                if (empty || feedback == null) {
                    setText(null);
                    setContextMenu(null);
                } else {
                    setText(feedback.toString());
                    if (feedback.getUserId() == currentUserId) {
                        ContextMenu contextMenu = new ContextMenu();
                        MenuItem editItem = new MenuItem("Chỉnh sửa");
                        MenuItem deleteItem = new MenuItem("Xóa");
                        editItem.setOnAction(e -> onEditFeedbackClick(feedback));
                        deleteItem.setOnAction(e -> onDeleteFeedbackClick(feedback));
                        contextMenu.getItems().addAll(editItem, deleteItem);
                        setContextMenu(contextMenu);
                    }
                }
            }
        });

        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            String genre = genreComboBox.getSelectionModel().getSelectedItem();
            songs = FXCollections.observableArrayList(songService.searchSongs(newValue, genre));
            songList.setItems(songs);
            System.out.println("🔍 Real-time search: " + songs.size() + " songs for title keyword='" + newValue + "', genre='" + genre + "'");
        });

        genreComboBox.getSelectionModel().selectedItemProperty().addListener((obs, old, newGenre) -> {
            String keyword = searchField.getText();
            songs = FXCollections.observableArrayList(songService.searchSongs(keyword, newGenre));
            songList.setItems(songs);
            System.out.println("🔍 Genre filter: " + songs.size() + " songs for title keyword='" + keyword + "', genre='" + newGenre + "'");
        });

        AutoCompleteTextField autoComplete = new AutoCompleteTextField(searchField, songService::getSearchSuggestions);

        addSongToPlaylistButton.setDisable(true);
        removeSongFromPlaylistButton.setDisable(true);
        addFeedbackButton.setDisable(true);
        searchButton.setDisable(false);
        clearSearchButton.setDisable(false);
    }


    public void setupResponsiveBindings(Stage stage) {
        // Left: songList, searchField, genreComboBox, playlistComboBox
        songList.prefWidthProperty().bind(stage.widthProperty().multiply(0.3));
        songList.prefHeightProperty().bind(stage.heightProperty().multiply(0.4));
        VBox.setVgrow(songList, javafx.scene.layout.Priority.ALWAYS);

        searchField.prefWidthProperty().bind(songList.widthProperty().multiply(0.9));
        searchField.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        genreComboBox.prefWidthProperty().bind(songList.widthProperty().multiply(0.9));
        genreComboBox.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        playlistComboBox.prefWidthProperty().bind(songList.widthProperty().multiply(0.9));
        playlistComboBox.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        createPlaylistButton.prefWidthProperty().bind(songList.widthProperty().multiply(0.9));
        createPlaylistButton.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        addSongToPlaylistButton.prefWidthProperty().bind(songList.widthProperty().multiply(0.45));
        addSongToPlaylistButton.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        removeSongFromPlaylistButton.prefWidthProperty().bind(songList.widthProperty().multiply(0.45));
        removeSongFromPlaylistButton.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));
        feedbackList.prefWidthProperty().bind(songList.widthProperty().multiply(0.9));
        feedbackList.prefHeightProperty().bind(stage.heightProperty().multiply(0.2));
        VBox.setVgrow(feedbackList, javafx.scene.layout.Priority.ALWAYS);
        addFeedbackButton.prefWidthProperty().bind(songList.widthProperty().multiply(0.9));
        addFeedbackButton.prefHeightProperty().bind(stage.heightProperty().multiply(0.05));

        // Center: coverImage, songTitle, songArtist, controlBox
        centerVBox.prefWidthProperty().bind(stage.widthProperty().multiply(0.6));
        centerVBox.spacingProperty().bind(stage.heightProperty().multiply(0.02));
        centerVBox.paddingProperty().bindBidirectional(new javafx.beans.property.SimpleObjectProperty<>(
                new Insets(stage.getHeight() * 0.02, stage.getWidth() * 0.05, stage.getHeight() * 0.02, stage.getWidth() * 0.05)));

        coverImage.fitWidthProperty().bind(centerVBox.widthProperty().multiply(0.7));
        coverImage.fitHeightProperty().bind(centerVBox.widthProperty().multiply(0.7));
        songTitle.prefWidthProperty().bind(centerVBox.widthProperty().multiply(0.9));
        songTitle.setWrapText(true);
        songArtist.prefWidthProperty().bind(centerVBox.widthProperty().multiply(0.9));
        songArtist.setWrapText(true);

        controlBox.prefWidthProperty().bind(centerVBox.widthProperty().multiply(0.9));
        controlBox.spacingProperty().bind(stage.widthProperty().multiply(0.01));
        playButton.prefWidthProperty().bind(controlBox.widthProperty().multiply(0.15));
        playButton.prefHeightProperty().bind(stage.heightProperty().multiply(0.04));
        playButton.setWrapText(true);
        pauseButton.prefWidthProperty().bind(controlBox.widthProperty().multiply(0.15));
        pauseButton.prefHeightProperty().bind(stage.heightProperty().multiply(0.04));
        pauseButton.setWrapText(true);
        prevButton.prefWidthProperty().bind(controlBox.widthProperty().multiply(0.15));
        prevButton.prefHeightProperty().bind(stage.heightProperty().multiply(0.04));
        prevButton.setWrapText(true);
        nextButton.prefWidthProperty().bind(controlBox.widthProperty().multiply(0.15));
        nextButton.prefHeightProperty().bind(stage.heightProperty().multiply(0.04));
        nextButton.setWrapText(true);
        shuffleButton.prefWidthProperty().bind(controlBox.widthProperty().multiply(0.15));
        shuffleButton.prefHeightProperty().bind(stage.heightProperty().multiply(0.04));
        shuffleButton.setWrapText(true);
        repeatButton.prefWidthProperty().bind(controlBox.widthProperty().multiply(0.15));
        repeatButton.prefHeightProperty().bind(stage.heightProperty().multiply(0.04));
        repeatButton.setWrapText(true);

        // Binding cho songDurationLabel trong bottom
        songDurationLabel.prefWidthProperty().bind(stage.widthProperty().multiply(0.6));
        songDurationLabel.setWrapText(false);

        // Log để debug kích thước
        controlBox.widthProperty().addListener((obs, old, newWidth) -> {
            System.out.println("📏 controlBox width: " + newWidth + ", button width: " + (newWidth.doubleValue() * 0.15));
        });
    }

    private void refreshPlaylists() {
        playlists = FXCollections.observableArrayList(songService.getUserPlaylists(currentUserId));
        playlists.add(0, new Playlist(0, "All Songs", 0));
        playlistComboBox.setItems(playlists);
        System.out.println("Refreshed playlists: " + playlists.size() + " playlists");
    }

    private void refreshGenres() {
        genres = FXCollections.observableArrayList(songService.getAllGenres());
        genres.add(0, "All Genres");
        genreComboBox.setItems(genres);
        System.out.println("Refreshed genres: " + genres.size() + " genres");
    }

    private void refreshSongList() {
        songs = FXCollections.observableArrayList(songService.searchSongs(null, null));
        songList.setItems(songs);
        System.out.println("Refreshed song list: " + songs.size() + " songs");
    }

    private void refreshSongList(int playlistId) {
        songs = FXCollections.observableArrayList(songService.getSongsInPlaylist(playlistId));
        songList.setItems(songs);
        System.out.println("Refreshed playlist song list: " + songs.size() + " songs for playlist_id=" + playlistId);
    }

    private void refreshFeedbackList(int songId) {
        feedbacks = FXCollections.observableArrayList(songService.getFeedbackForSong(songId));
        feedbackList.setItems(feedbacks);
        System.out.println("Refreshed feedback list: " + feedbacks.size() + " feedback for song_id=" + songId);
    }

    private void initializeAdminFeatures() {
        String role = getUserRole();
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(javafx.geometry.Pos.CENTER);

        if ("admin".equals(role)) {
            Button adminButton = new Button("Admin Panel");
            adminButton.setStyle("-fx-font-size:14px;");
            adminButton.setOnAction(e -> {
                try {
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        System.out.println("Đã dừng MediaPlayer trước khi mở Admin Panel");
                    }

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/smartmusicplayer/views/admin-panel.fxml"));
                    Scene scene = new Scene(loader.load());
                    AdminController controller = loader.getController();
                    controller.setCurrentUserId(currentUserId);
                    controller.setMediaPlayer(mediaPlayer);
                    Stage stage = (Stage) songList.getScene().getWindow();
                    loadScene(stage, scene, "Admin Panel - Smart Music Player");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Mở Admin Panel thất bại: " + ex.getMessage());
                    alert.show();
                }
            });
            buttonContainer.getChildren().add(adminButton);
        }

        Button logoutButton = new Button("Đăng Xuất");
        logoutButton.setStyle("-fx-font-size:14px;");
        logoutButton.setOnAction(e -> onLogoutClick());
        buttonContainer.getChildren().add(logoutButton);

        if (centerVBox != null) {
            centerVBox.getChildren().add(buttonContainer);
            System.out.println("Đã thêm nút " + (role.equals("admin") ? "Admin Panel và Đăng Xuất" : "Đăng Xuất"));
        } else {
            System.out.println("Không thể thêm nút: centerVBox là null");
        }
    }

    private String getUserRole() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT role FROM users WHERE id = ?")) {
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "user";
    }

    private void loadSong(Song song) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }

            String filePath = "src/main/resources/com/example/smartmusicplayer/songs/" + song.getFilePath().replace("songs/", "");
            File songFile = new File(filePath);
            if (!songFile.exists()) {
                String errorMsg = "Cannot find song file: " + song.getFilePath() + " at path: " + filePath;
                System.out.println(errorMsg);
                Alert alert = new Alert(Alert.AlertType.ERROR, errorMsg);
                alert.show();
                return;
            }

            Media media = new Media(songFile.toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            songTitle.setText(song.getTitle());
            songArtist.setText(song.getArtist() != null ? song.getArtist() : "Unknown");

            String coverPath = song.getCoverImage() != null ? "src/main/resources/com/example/smartmusicplayer/covers/" + song.getCoverImage() : null;
            File coverFile = coverPath != null ? new File(coverPath) : null;
            if (coverFile != null && coverFile.exists()) {
                coverImage.setImage(new Image(coverFile.toURI().toString()));
            } else {
                coverImage.setImage(null);
                System.out.println("No cover image for: " + song.getTitle());
            }

            // Cập nhật progressBar
            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (!progressBar.isValueChanging()) {
                    progressBar.setValue(newTime.toSeconds());
                    // Cập nhật thời gian hiện tại/tổng thời gian
                    int currentMinutes = (int) newTime.toMinutes();
                    int currentSeconds = (int) (newTime.toSeconds() % 60);
                    int totalMinutes = (int) mediaPlayer.getTotalDuration().toMinutes();
                    int totalSeconds = (int) (mediaPlayer.getTotalDuration().toSeconds() % 60);
                    songDurationLabel.setText(String.format("%d:%02d/%d:%02d", currentMinutes, currentSeconds, totalMinutes, totalSeconds));
                }
            });

            mediaPlayer.setOnReady(() -> {
                Duration duration = media.getDuration();
                progressBar.setMax(duration.toSeconds());
                // Khởi tạo thời gian ban đầu
                int totalMinutes = (int) duration.toMinutes();
                int totalSeconds = (int) (duration.toSeconds() % 60);
                songDurationLabel.setText(String.format("0:00/%d:%02d", totalMinutes, totalSeconds));
                System.out.println("⏱️ Total duration: " + String.format("%d:%02d", totalMinutes, totalSeconds));
            });

            // Hỗ trợ tua bài hát
            progressBar.valueProperty().addListener((obs, oldValue, newValue) -> {
                if (progressBar.isValueChanging()) {
                    mediaPlayer.seek(Duration.seconds(newValue.doubleValue()));
                    System.out.println("⏩ Seek to: " + newValue.doubleValue() + " seconds");
                }
            });
            progressBar.setOnMousePressed(event -> progressBar.setValueChanging(true));
            progressBar.setOnMouseReleased(event -> progressBar.setValueChanging(false));

            mediaPlayer.setOnEndOfMedia(() -> {
                if (repeatMode == 1) {
                    mediaPlayer.seek(Duration.ZERO);
                    mediaPlayer.play();
                    System.out.println("🔁 Lặp lại bài: " + song.getTitle());
                } else if (repeatMode == 2) {
                    onNextClick();
                } else {
                    if (currentIndex < songs.size() - 1) {
                        onNextClick();
                    } else {
                        mediaPlayer.stop();
                        System.out.println("⏹️ Hết danh sách, dừng phát");
                    }
                }
            });

            System.out.println("Loaded: " + song.getTitle());
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to load song: " + e.getMessage());
            alert.show();
        }
    }

    @FXML
    private void onCreatePlaylistClick() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Tạo Playlist Mới");
        dialog.setHeaderText("Nhập tên playlist:");
        dialog.setContentText("Tên:");
        dialog.showAndWait().ifPresent(name -> {
            if (name.trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Tên playlist không được để trống!");
                alert.show();
                return;
            }
            if (songService.createPlaylist(name, currentUserId)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Tạo playlist thành công!");
                alert.show();
                refreshPlaylists();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Tạo playlist thất bại!");
                alert.show();
            }
        });
    }

    @FXML
    private void onAddSongToPlaylistClick() {
        Song selectedSong = songList.getSelectionModel().getSelectedItem();
        if (selectedSong == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Vui lòng chọn một bài hát!");
            alert.show();
            return;
        }

        ChoiceDialog<Playlist> dialog = new ChoiceDialog<>(null, playlists);
        dialog.setTitle("Thêm vào Playlist");
        dialog.setHeaderText("Chọn playlist để thêm bài hát: " + selectedSong.getTitle());
        dialog.setContentText("Playlist:");
        dialog.showAndWait().ifPresent(playlist -> {
            if (playlist != null && playlist.getId() != 0) {
                if (songService.addSongToPlaylist(playlist.getId(), selectedSong.getId())) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Đã thêm bài hát vào playlist!");
                    alert.show();
                    if (playlistComboBox.getSelectionModel().getSelectedItem() != null &&
                            playlistComboBox.getSelectionModel().getSelectedItem().getId() == playlist.getId()) {
                        refreshSongList(playlist.getId());
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Thêm bài hát thất bại!");
                    alert.show();
                }
            }
        });
    }

    @FXML
    private void onRemoveSongFromPlaylistClick() {
        Song selectedSong = songList.getSelectionModel().getSelectedItem();
        Playlist selectedPlaylist = playlistComboBox.getSelectionModel().getSelectedItem();
        if (selectedSong == null || selectedPlaylist == null || selectedPlaylist.getId() == 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Vui lòng chọn một bài hát và playlist!");
            alert.show();
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Xóa '" + selectedSong.getTitle() + "' khỏi playlist '" + selectedPlaylist.getName() + "'?");
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (songService.removeSongFromPlaylist(selectedPlaylist.getId(), selectedSong.getId())) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Đã xóa bài hát khỏi playlist!");
                    alert.show();
                    refreshSongList(selectedPlaylist.getId());
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Xóa bài hát thất bại!");
                    alert.show();
                }
            }
        });
    }

    @FXML
    private void onAddFeedbackClick() {
        Song selectedSong = songList.getSelectionModel().getSelectedItem();
        if (selectedSong == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Vui lòng chọn một bài hát!");
            alert.show();
            return;
        }

        Dialog<Feedback> dialog = new Dialog<>();
        dialog.setTitle("Đánh giá Bài Hát");
        dialog.setHeaderText("Đánh giá bài hát: " + selectedSong.getTitle());

        ButtonType submitButtonType = new ButtonType("Gửi", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        ChoiceBox<Integer> ratingChoice = new ChoiceBox<>(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        ratingChoice.setValue(5);
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Nhập bình luận (tùy chọn)");
        commentArea.setPrefRowCount(3);
        content.getChildren().addAll(new Label("Điểm (1-5 sao):"), ratingChoice, new Label("Bình luận:"), commentArea);

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                return new Feedback(0, currentUserId, selectedSong.getId(), ratingChoice.getValue(), commentArea.getText(), null, null);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(feedback -> {
            if (songService.addFeedback(feedback.getUserId(), feedback.getSongId(), feedback.getRating(), feedback.getComment())) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Đã gửi đánh giá!");
                alert.show();
                refreshFeedbackList(selectedSong.getId());
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Gửi đánh giá thất bại!");
                alert.show();
            }
        });
    }

    private void onEditFeedbackClick(Feedback feedback) {
        Dialog<Feedback> dialog = new Dialog<>();
        dialog.setTitle("Chỉnh sửa Đánh giá");
        dialog.setHeaderText("Chỉnh sửa đánh giá cho bài hát");

        ButtonType submitButtonType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        ChoiceBox<Integer> ratingChoice = new ChoiceBox<>(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        ratingChoice.setValue(feedback.getRating());
        TextArea commentArea = new TextArea();
        commentArea.setText(feedback.getComment() != null ? feedback.getComment() : "");
        commentArea.setPromptText("Nhập bình luận (tùy chọn)");
        commentArea.setPrefRowCount(3);
        content.getChildren().addAll(new Label("Điểm (1-5 sao):"), ratingChoice, new Label("Bình luận:"), commentArea);

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                return new Feedback(feedback.getId(), feedback.getUserId(), feedback.getSongId(), ratingChoice.getValue(), commentArea.getText(), null, null);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedFeedback -> {
            if (songService.updateFeedback(updatedFeedback.getId(), updatedFeedback.getUserId(), updatedFeedback.getRating(), updatedFeedback.getComment())) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Đã cập nhật đánh giá!");
                alert.show();
                refreshFeedbackList(feedback.getSongId());
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Cập nhật đánh giá thất bại!");
                alert.show();
            }
        });
    }



    private void onDeleteFeedbackClick(Feedback feedback) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Xóa đánh giá này?");
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (songService.deleteFeedback(feedback.getId(), feedback.getUserId())) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Đã xóa đánh giá!");
                    alert.show();
                    refreshFeedbackList(feedback.getSongId());
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Xóa đánh giá thất bại!");
                    alert.show();
                }
            }
        });
    }

    // Sửa phương thức onSearchClick
    @FXML
    private void onSearchClick() {
        String keyword = searchField.getText().trim();
        String genre = genreComboBox.getSelectionModel().getSelectedItem();
        songs = FXCollections.observableArrayList(songService.searchSongs(keyword, genre));
        songList.setItems(songs);
        System.out.println("🔍 Button search: " + songs.size() + " songs for keyword='" + keyword + "', genre='" + genre + "'");
        if (songs.isEmpty() && ((keyword != null && !keyword.isEmpty()) || (genre != null && !genre.equals("All Genres")))) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thông báo");
            alert.setHeaderText(null);
            alert.setContentText("Không tìm thấy bài hát nào!");
            alert.showAndWait(); // Đảm bảo thông báo đóng được
        }
    }

    @FXML
    private void onClearSearchClick() {
        searchField.clear();
        genreComboBox.getSelectionModel().clearSelection();
        refreshSongList();
        System.out.println("Cleared search filters");
    }

    @FXML
    private void onPlayClick() {
        if (mediaPlayer != null) mediaPlayer.play();
        System.out.println("Đang phát: " + songTitle.getText());
    }

    @FXML
    private void onPauseClick() {
        if (mediaPlayer != null) mediaPlayer.pause();
        System.out.println("Đã tạm dừng: " + songTitle.getText());
    }

    @FXML
    private void onNextClick() {
        if (songs == null || songs.isEmpty()) return;
        if (repeatMode == 1) { // Lặp một bài
            mediaPlayer.seek(javafx.util.Duration.ZERO);
            mediaPlayer.play();
            System.out.println("🔁 Lặp lại bài: " + songs.get(currentIndex).getTitle());
        } else {
            currentIndex = (currentIndex + 1) % songs.size();
            Song next = songs.get(currentIndex);
            songList.getSelectionModel().select(next);
            loadSong(next);
            mediaPlayer.play();
            System.out.println("Chuyển bài tiếp theo: " + next.getTitle());
        }
    }

    @FXML
    private void onPrevClick() {
        if (songs == null || songs.isEmpty()) return;
        if (repeatMode == 1) { // Lặp một bài
            mediaPlayer.seek(javafx.util.Duration.ZERO);
            mediaPlayer.play();
            System.out.println("🔁 Lặp lại bài: " + songs.get(currentIndex).getTitle());
        } else {
            currentIndex = (currentIndex - 1 + songs.size()) % songs.size();
            Song prev = songs.get(currentIndex);
            songList.getSelectionModel().select(prev);
            loadSong(prev);
            mediaPlayer.play();
            System.out.println("Chuyển bài trước: " + prev.getTitle());
        }
    }

    @FXML
    private void onShuffleClick() {
        if (songs == null || songs.isEmpty()) return;
        Random random = new Random();
        currentIndex = random.nextInt(songs.size());
        Song randomSong = songs.get(currentIndex);
        songList.getSelectionModel().select(randomSong);
        loadSong(randomSong);
        mediaPlayer.play();
        System.out.println("🔀 Phát ngẫu nhiên: " + randomSong.getTitle());
    }


    // Sửa phương thức onRepeatClick
    @FXML
    private void onRepeatClick() {
        repeatMode = (repeatMode + 1) % 3;
        switch (repeatMode) {
            case 0:
                repeatButton.setText("Repeat");
                System.out.println("🔁 Tắt chế độ lặp");
                break;
            case 1:
                repeatButton.setText("Repeat 1");
                System.out.println("🔁 Bật lặp một bài");
                break;
            case 2:
                repeatButton.setText("Repeat All");
                System.out.println("🔁 Bật lặp playlist");
                break;
        }
    }


    @FXML
    private void onLogoutClick() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                System.out.println("Đã dừng MediaPlayer trước khi đăng xuất");
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/smartmusicplayer/views/login.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) songList.getScene().getWindow();
            loadScene(stage, scene, "Smart Music Player");
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Đăng xuất thất bại: " + ex.getMessage());
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
}