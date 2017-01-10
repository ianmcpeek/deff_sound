package com.example.ian.deffsound.musiclist;

import com.example.ian.deffsound.MusicCategory;

/**
 * Created by Ian on 1/5/2017.
 */
public interface MusicItem {
    boolean isSong();
    String getTitle();
    MusicCategory getCategory();
}
