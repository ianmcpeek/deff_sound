package com.example.ian.deffsound;

import android.provider.MediaStore;

import java.util.Stack;

/**
 * Created by ian on 10/01/17.
 */

public class MusicDirectoryQueryHistory {

    private Stack<MusicDirectoryQuery> queryHistory;
    private MusicDirectoryQuery currentDirectoryQuery;

    public MusicDirectoryQueryHistory(MusicDirectoryQuery query) {
        currentDirectoryQuery = query;
        queryHistory = new Stack<MusicDirectoryQuery>();
    }

    public MusicDirectoryQuery getCurrentDirectoryQuery() {
        return currentDirectoryQuery;
    }

    public void addToHistory(MusicDirectoryQuery query) {
        //check if snapshot identical to previous entry
        if(currentDirectoryQuery.equals(query)) return;
        queryHistory.push(currentDirectoryQuery);
        currentDirectoryQuery = query;
        //displayListTitle(snapshot);
    }

    public MusicDirectoryQuery removeFromHistory() {
        if(queryHistory.isEmpty()) return null;
        MusicDirectoryQuery query = queryHistory.pop();
        //querySongs(snapshot);
        currentDirectoryQuery = query;
        //displayListTitle(snapshot);
        return currentDirectoryQuery;
    }

    public boolean isEmpty() {
        return queryHistory.isEmpty();
    }
}
