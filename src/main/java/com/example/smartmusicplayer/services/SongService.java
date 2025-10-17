package com.example.smartmusicplayer.services;

import com.example.smartmusicplayer.models.Feedback;
import com.example.smartmusicplayer.models.Playlist;
import com.example.smartmusicplayer.models.Song;
import com.example.smartmusicplayer.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SongService {
    public List<Song> getAllSongs() {
        List<Song> songs = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM songs");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                songs.add(new Song(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("artist"),
                        rs.getString("album"),
                        rs.getString("genre"),
                        rs.getString("file_path"),
                        rs.getString("cover_image")
                ));
            }
            System.out.println("🎵 Fetched " + songs.size() + " songs from database");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return songs;
    }

    public List<Song> searchSongs(String keyword, String genre) {
        List<Song> songs = new ArrayList<>();
        String query = "SELECT id, title, artist, album, genre, file_path, cover_image " +
                "FROM songs WHERE (? IS NULL OR ? = '' OR LOWER(title) LIKE LOWER(?)) " +
                "AND (? IS NULL OR ? = 'All Genres' OR genre = ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            String searchPattern = (keyword != null && !keyword.trim().isEmpty()) ? "%" + keyword.trim() + "%" : "";
            stmt.setString(1, keyword);
            stmt.setString(2, keyword);
            stmt.setString(3, searchPattern);
            stmt.setString(4, genre);
            stmt.setString(5, genre);
            stmt.setString(6, genre != null && !genre.equals("All Genres") ? genre : null);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    songs.add(new Song(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("artist"),
                            rs.getString("album"),
                            rs.getString("genre"),
                            rs.getString("file_path"),
                            rs.getString("cover_image")
                    ));
                }
            }
            System.out.println("🔍 Fetched " + songs.size() + " songs for title keyword='" + keyword + "', genre='" + genre + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return songs;
    }

    public List<String> getAllGenres() {
        List<String> genres = new ArrayList<>(List.of(
                "Pop", "Rock", "Hip-Hop/Rap", "Jazz", "Classical", "Electronic", "Country",
                "R&B/Soul", "Reggae", "Blues", "Folk", "Metal", "Punk", "Indie", "Alternative",
                "EDM", "Latin", "K-Pop", "World Music", "Gospel", "Funk", "Disco", "House",
                "Techno", "Trance"
        ));
        System.out.println("🎸 Loaded " + genres.size() + " fixed genres");
        return genres;
    }

    public List<Playlist> getUserPlaylists(int userId) {
        List<Playlist> playlists = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, name, user_id FROM playlists WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    playlists.add(new Playlist(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("user_id")
                    ));
                }
            }
            System.out.println("📚 Fetched " + playlists.size() + " playlists for user_id=" + userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return playlists;
    }

    public List<Song> getSongsInPlaylist(int playlistId) {
        List<Song> songs = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT s.* FROM songs s JOIN playlist_songs ps ON s.id = ps.song_id WHERE ps.playlist_id = ? ORDER BY ps.order_index")) {
            stmt.setInt(1, playlistId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    songs.add(new Song(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("artist"),
                            rs.getString("album"),
                            rs.getString("genre"),
                            rs.getString("file_path"),
                            rs.getString("cover_image")
                    ));
                }
            }
            System.out.println("🎵 Fetched " + songs.size() + " songs for playlist_id=" + playlistId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return songs;
    }

    public boolean updateSongOrderInPlaylist(int playlistId, int songId, int newOrderIndex) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE playlist_songs SET order_index = ? WHERE playlist_id = ? AND song_id = ?")) {
            stmt.setInt(1, newOrderIndex);
            stmt.setInt(2, playlistId);
            stmt.setInt(3, songId);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("📚 Updated order_index for song_id=" + songId + " in playlist_id=" + playlistId + " to " + newOrderIndex);
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createPlaylist(String name, int userId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO playlists (name, user_id) VALUES (?, ?)")) {
            stmt.setString(1, name);
            stmt.setInt(2, userId);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("📚 Created playlist: " + name + ", rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addSongToPlaylist(int playlistId, int songId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO playlist_songs (playlist_id, song_id) VALUES (?, ?)")) {
            stmt.setInt(1, playlistId);
            stmt.setInt(2, songId);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("🎵 Added song_id=" + songId + " to playlist_id=" + playlistId + ", rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeSongFromPlaylist(int playlistId, int songId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM playlist_songs WHERE playlist_id = ? AND song_id = ?")) {
            stmt.setInt(1, playlistId);
            stmt.setInt(2, songId);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("🗑️ Removed song_id=" + songId + " from playlist_id=" + playlistId + ", rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Feedback> getFeedbackForSong(int songId) {
        List<Feedback> feedbackList = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT f.*, u.username FROM feedback f JOIN users u ON f.user_id = u.id WHERE f.song_id = ?")) {
            stmt.setInt(1, songId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    feedbackList.add(new Feedback(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getInt("song_id"),
                            rs.getInt("rating"),
                            rs.getString("comment"),
                            rs.getTimestamp("created_at"),
                            rs.getString("username")
                    ));
                }
            }
            System.out.println("📝 Fetched " + feedbackList.size() + " feedback for song_id=" + songId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return feedbackList;
    }

    public boolean addFeedback(int userId, int songId, int rating, String comment) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO feedback (user_id, song_id, rating, comment) VALUES (?, ?, ?, ?)")) {
            stmt.setInt(1, userId);
            stmt.setInt(2, songId);
            stmt.setInt(3, rating);
            stmt.setString(4, comment != null && !comment.trim().isEmpty() ? comment : null);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("📝 Added feedback for song_id=" + songId + " by user_id=" + userId + ", rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateFeedback(int feedbackId, int userId, int rating, String comment) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE feedback SET rating = ?, comment = ? WHERE id = ? AND user_id = ?")) {
            stmt.setInt(1, rating);
            stmt.setString(2, comment != null && !comment.trim().isEmpty() ? comment : null);
            stmt.setInt(3, feedbackId);
            stmt.setInt(4, userId);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("📝 Updated feedback_id=" + feedbackId + " for user_id=" + userId + ", rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteFeedback(int feedbackId, int userId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM feedback WHERE id = ? AND user_id = ?")) {
            stmt.setInt(1, feedbackId);
            stmt.setInt(2, userId);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("🗑️ Deleted feedback_id=" + feedbackId + " for user_id=" + userId + ", rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getSearchSuggestions(String keyword) {
        List<String> suggestions = new ArrayList<>();
        String query = "SELECT title FROM songs WHERE LOWER(title) LIKE LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            String searchPattern = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() + "%" : "%";
            stmt.setString(1, searchPattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String title = rs.getString("title");
                    suggestions.add(title);
                }
            }
            System.out.println("🔍 Loaded " + suggestions.size() + " search suggestions for title keyword='" + keyword + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return suggestions.stream().distinct().collect(Collectors.toList());
    }

}