package com.example.ian.supermix;

/**
 * Created by ian on 06/01/17.
 */

public enum MusicDirectoryType {
    ARTIST,
    ALBUM,
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

    public static MusicDirectoryType parseString(String type) {
        if(type.equals(toString(MusicDirectoryType.ARTIST))) {
            return MusicDirectoryType.ARTIST;
        } else if(type.equals(toString(MusicDirectoryType.ALBUM))) {
            return MusicDirectoryType.ALBUM;
        } else {
            return MusicDirectoryType.SONG;
        }
    }

    public static String getStringFromInt(int type) {
        MusicDirectoryType directoryType;
        if(type == 0) directoryType = MusicDirectoryType.ARTIST;
        else if(type == 1) directoryType = MusicDirectoryType.ALBUM;
        else directoryType = MusicDirectoryType.SONG;
        return MusicDirectoryType.toString(directoryType);
    }
}

