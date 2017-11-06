package com.example.ian.supermix.musiclist;

import com.example.ian.supermix.MusicDirectoryType;

/**
 * Created by Ian on 1/5/2017.
 */
public interface MusicItem {
    boolean isSong();
    String getTitle();
    MusicDirectoryType getCategory();
}
