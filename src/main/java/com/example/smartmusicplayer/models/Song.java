package com.example.smartmusicplayer.models;

public class Song {
    private int id;
    private String title;
    private String artist;
    private String album;
    private String genre;
    private String filePath;
    private String coverImage;

    public Song(int id, String title, String artist, String album, String genre, String filePath, String coverImage) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
        this.filePath = filePath;
        this.coverImage = coverImage;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public String getGenre() { return genre; }
    public String getFilePath() { return filePath; }
    public String getCoverImage() { return coverImage; }

    @Override
    public String toString() {
        return title + (artist != null ? " - " + artist : "");
    }
}