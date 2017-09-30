package com.example.ian.deffsound.songqueue;

import com.example.ian.deffsound.songview.Song;

import java.util.ArrayList;

/**
 * Created by ian on 7/26/2017.
 */

public class SongPlaylist {
    private int position;
    private ArrayList<Song> playlist;

    public SongPlaylist(int position, ArrayList<Song> playlist) {
        this.position = position;
        this.playlist = playlist;
    }

    public SongPlaylist(ArrayList<Song> playlist) {
        this.position = 0;
        this.playlist = playlist;
    }

    public SongPlaylist() {
        this.position = 0;
        this.playlist = new ArrayList<>();
    }

    public int getCurrentSongPosition() {
        return position;
    }

    public Song getSong(int position) {
        if(position >= 0 && position < playlist.size()) {
            return playlist.get(position);
        }
        return null;
    }

    public Song getCurrentSong() {
        if(position >= playlist.size()) {
            return null;
        }
        return playlist.get(position);
    }

    public void setCurrentSong(int position) {
        if(position >= 0 && position < playlist.size()) {
            this.position = position;
        }
    }

    public void removeSong(int index) {
        if(index >= 0 && index < playlist.size()) {
            playlist.remove(index);
        }
    }

    public void addSong(Song song) {
        playlist.add(song);
    }

    public Song previousSong() {
        if(position > 0) {
            position--;
        }
        return getCurrentSong();
    }

    public Song nextSong() {
        if(position <= playlist.size()) {
            position++;
        }
        return getCurrentSong();
    }

    public ArrayList<Song> getPlaylist() {
        return playlist;
    }

    public int size() {
        return playlist.size();
    }

    public boolean isEmpty() {
        return playlist.size() == 0;
    }
}
