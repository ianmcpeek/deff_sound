package com.example.ian.deffsound;

import android.net.Uri;
import android.provider.MediaStore;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ian on 12/15/2016.
 */
public class CategoryFilter {
    private Map<CATEGORY, CategoryData> filterData;


    private CategoryFilter() {
        filterData = new HashMap<CATEGORY, CategoryData>();
        //artist category
        filterData.put(CATEGORY.ARTISTS,
                new CategoryData(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Artists._ID,
                            MediaStore.Audio.Artists.ARTIST,
                            MediaStore.Audio.Artists.NUMBER_OF_ALBUMS},
                        "Artists", ""));
        filterData.put(CATEGORY.ALBUMS,
                new CategoryData(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        new String[]{MediaStore.Audio.Albums._ID,
                                MediaStore.Audio.Albums.ALBUM,
                                MediaStore.Audio.Albums.NUMBER_OF_SONGS,
                                MediaStore.Audio.Albums.FIRST_YEAR},
                        "Albums", ""));
        filterData.put(CATEGORY.SONGS,
                new CategoryData(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        new String[]{MediaStore.Audio.Media._ID,
                                MediaStore.Audio.Media.TITLE,
                                MediaStore.Audio.Media.ARTIST,
                                MediaStore.Audio.Media.ALBUM,
                                MediaStore.Audio.Media.TRACK},
                        "Songs", MediaStore.Audio.Media.IS_MUSIC + "!=0"));
    }

    private CategoryFilter filter = new CategoryFilter();

    public CategoryFilter getInstance() {return filter;}

    public CategoryData getCategory(CATEGORY category) {
        CategoryData data = null;
        switch(category) {
            case ARTISTS:
                data = filterData.get(CATEGORY.ARTISTS);
                break;
            case ALBUMS:
                data = filterData.get(CATEGORY.ALBUMS);
                break;
            case SONGS:
                data = filterData.get(CATEGORY.SONGS);
                break;
        }
        return data;
    }

    public class CategoryData {
        private Uri uri;
        private String[] columns;
        private String title;
        private String selection;

        private CategoryData(Uri uri, String[] columns, String title, String selection) {
            this.uri = uri;
            this.columns = columns;
            this.title = title;
            this.selection = selection;
        }

        public Uri getUri() { return uri; }
        public String[] getColumns() { return columns; }
        public String getTitle() { return title; }
    }

    public enum CATEGORY {
        ARTISTS,
        ALBUMS,
        SONGS
    }
}
