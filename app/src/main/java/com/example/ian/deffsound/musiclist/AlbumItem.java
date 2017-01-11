package com.example.ian.deffsound.musiclist;

import com.example.ian.deffsound.MusicDirectoryType;

/**
 * Created by ian on 06/01/17.
 */

public class AlbumItem implements MusicItem {
    private String title;
    private int trackCount;
    private String yearReleased;
    //eventually used to retrieve album cover
    private String imageUri;

    public AlbumItem(String title, int trackCount, String yearReleased) {
        this.title = title;
        this.trackCount = trackCount;
        this.yearReleased = yearReleased;
        this.imageUri = null;
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


}
