package com.example.ian.deffsound.musiclist;

import com.example.ian.deffsound.CategoryFilter;

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

    public int getAlbumCount() {
        return albumCount;
    }

    @Override
    public CategoryFilter.CATEGORY getCategory() {
        return null;
    }
}
