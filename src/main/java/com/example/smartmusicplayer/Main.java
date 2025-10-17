package com.example.smartmusicplayer;

import com.example.smartmusicplayer.models.Song;
import com.example.smartmusicplayer.services.SongService;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        SongService service = new SongService();
        List<Song> songs = service.getAllSongs();

        if (songs.isEmpty()) {
            System.out.println("❌ Không có bài hát nào trong DB!");
        } else {
            System.out.println("✅ Danh sách bài hát:");
            for (Song s : songs) {
                System.out.println(" - " + s);
            }
        }
    }
}
