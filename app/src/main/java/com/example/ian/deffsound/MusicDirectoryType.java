package com.example.ian.deffsound;

/**
 * Created by ian on 06/01/17.
 */

public enum MusicDirectoryType {
    ARTIST,
    ALBUM,
    PLAYLIST,
    SONG;

    public static String toString(MusicDirectoryType type) {
        String s = "";
        switch(type) {
            case ARTIST:
                s = "Artist";
                break;
            case ALBUM:
                s = "Album";
                break;
            case SONG:
                s = "Song";
                break;
        }
        return s;
    }
}

