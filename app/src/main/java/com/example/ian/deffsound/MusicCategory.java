package com.example.ian.deffsound;

/**
 * Created by ian on 06/01/17.
 */

public enum MusicCategory {
    ARTIST,
    ALBUM,
    PLAYLIST,
    SONG;

    public static String getCategoryString(MusicCategory cat) {
        String s = "";
        switch(cat) {
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

