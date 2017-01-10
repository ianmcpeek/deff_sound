package com.example.ian.deffsound;

import android.util.Log;

/**
 * Created by ian on 09/01/17.
 */
public class HistorySnapShot {
    private MusicCategory category;
    private String selection;
    private String[] selectionArgs;
    private String orderBy;

    public HistorySnapShot(MusicCategory category, String selection,
                           String[] selectionArgs, String orderBy) {
        this.category = category;
        this.selection = selection;
        this.selectionArgs = selectionArgs;
        this.orderBy = orderBy;
    }

    public MusicCategory getCategory() {
        return category;
    }

    public String getSelection() {
        return selection;
    }

    public String[] getSelectionArgs() {
        return selectionArgs;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public String getSnapshotTitle() {
        String title = MusicCategory.getCategoryString(category);
        Log.e("SNAPSHOT", title);
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null) return false;
        if (this == o) return true;
        if (!(o instanceof HistorySnapShot)) return false;
        HistorySnapShot s = (HistorySnapShot) o;
        return this.getSnapshotTitle().equals(s.getSnapshotTitle());
    }
}