package com.example.ian.supermix;

/**
 * Created by ian on 09/01/17.
 */
public class MusicDirectoryQuery {
    private MusicDirectoryType parentType;
    private MusicDirectoryType type;
    private String title;

    public MusicDirectoryQuery(MusicDirectoryType parentType, MusicDirectoryType type, String title) {
        this.parentType = parentType;
        this.type = type;
        this.title = title;
    }

    public MusicDirectoryType getParentDirectoryType() { return parentType; }

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
        if(q.getTitle() == null && this.getTitle() != null) return false;
        if(q.getTitle() != null && this.getTitle() == null) return false;
        if(q.getTitle() == null && this.getTitle() == null) return this.getMusicDirectoryType() == q.getMusicDirectoryType();
        return this.getMusicDirectoryType() == q.getMusicDirectoryType() &&
                this.getTitle().equals(q.getTitle());
    }
}