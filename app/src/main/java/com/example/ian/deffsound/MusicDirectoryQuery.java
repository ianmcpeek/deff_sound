package com.example.ian.deffsound;

import android.util.Log;

/**
 * Created by ian on 09/01/17.
 */
public class MusicDirectoryQuery {
    private MusicDirectoryType type;
    private String title;

    public MusicDirectoryQuery(MusicDirectoryType type, String title) {
        this.type = type;
        this.title = title;
    }

    public MusicDirectoryType getMusicDirectoryType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public boolean hasQueryTitle() {
        return title != null;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null) return false;
        if (this == o) return true;
        if (!(o instanceof MusicDirectoryQuery)) return false;
        MusicDirectoryQuery q = (MusicDirectoryQuery) o;
        return this.getMusicDirectoryType() == q.getMusicDirectoryType() &&
                this.getTitle().equals(q.getTitle());
    }
}