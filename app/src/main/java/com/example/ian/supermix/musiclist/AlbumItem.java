package com.example.ian.supermix.musiclist;

import com.example.ian.supermix.MusicDirectoryType;

/**
 * Created by ian on 06/01/17.
 */

public class AlbumItem implements MusicItem {
    private String title;
    private int trackCount;
    private String yearReleased;
    private String albumArt;

    public AlbumItem(String title, int trackCount, String yearReleased, String albumArt) {
        this.title = title;
        this.trackCount = trackCount;
        this.yearReleased = yearReleased;
        this.albumArt = albumArt;
    }

    @Override
    public boolean isSong() {
        return false;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public MusicDirectoryType getCategory() { return MusicDirectoryType.ALBUM; }

    public int getTrackCount() {
        return trackCount;
    }

    public String getYear() { return yearReleased; }

    public String getAlbumArt() { return albumArt; }


}
