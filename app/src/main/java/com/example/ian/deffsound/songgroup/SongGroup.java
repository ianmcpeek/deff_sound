package com.example.ian.deffsound.songgroup;

import com.example.ian.deffsound.songview.Song;

import java.util.ArrayList;

/**
 * Created by Ian on 12/4/2016.
 */
public class SongGroup {
    private ArrayList<Song> songs;
    private long id;
    private String title;
    private String imageSource;
    private int tracks;
    private GroupType type;

    public SongGroup(long id, String title, ArrayList<Song> songs,
                     int tracks, GroupType type) {
        this.id = id;
        this.title = title;
        this.songs = songs;
        this.imageSource = null;
        this.tracks = tracks;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public int getTotalTracks() {
        return tracks;
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }

    public GroupType getType() { return type; }

    public enum GroupType {
        ALBUM,
        ARTIST,
        PLAYLIST
    }
}
