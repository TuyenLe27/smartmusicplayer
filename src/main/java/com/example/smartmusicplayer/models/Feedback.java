package com.example.smartmusicplayer.models;

import java.sql.Timestamp;

public class Feedback {
    private int id;
    private int userId;
    private int songId;
    private int rating;
    private String comment;
    private Timestamp createdAt;
    private String username; // Để hiển thị tên người dùng

    public Feedback(int id, int userId, int songId, int rating, String comment, Timestamp createdAt, String username) {
        this.id = id;
        this.userId = userId;
        this.songId = songId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.username = username;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getSongId() { return songId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public Timestamp getCreatedAt() { return createdAt; }
    public String getUsername() { return username; }

    @Override
    public String toString() {
        return username + ": " + rating + " sao - " + (comment != null ? comment : "") + " (" + createdAt + ")";
    }
}