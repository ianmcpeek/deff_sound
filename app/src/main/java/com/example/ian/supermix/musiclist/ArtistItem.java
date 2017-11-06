package com.example.ian.supermix.musiclist;

import com.example.ian.supermix.MusicDirectoryType;

/**
 * Created by Ian on 1/6/2017.
 */
public class ArtistItem implements MusicItem {

    private String title;
    private int albumCount;
    //eventually used to retrieve artist image
    private String imageUri;

    public ArtistItem(String title, int albumCount) {
        this.title = title;
        this.albumCount= albumCount;
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
    public MusicDirectoryType getCategory() { return MusicDirectoryType.ARTIST; }

    public int getAlbumCount() {
        return albumCount;
    }

}
