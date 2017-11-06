package com.example.ian.supermix;

import java.util.Stack;

/**
 * Created by ian on 10/01/17.
 */

public class MusicDirectoryQueryHistory {
    private static Stack<MusicDirectoryQuery> queryHistory = new Stack<>();
    private static MusicDirectoryQuery currentQuery = null;

    public static void resetQueryHistory(MusicDirectoryQuery query) {
        queryHistory = new Stack<>();
        currentQuery = query;
    }

    public static MusicDirectoryQuery getQuery() {
        return currentQuery;
    }

    public static void addToHistory(MusicDirectoryQuery query) {
        //check if snapshot identical to previous entry
        if(getQuery() != null
            && getQuery().equals(query)) {
            return;
        }
        queryHistory.push(currentQuery);
        currentQuery = query;
    }

    public static MusicDirectoryQuery removeFromHistory() {
        if(queryHistory.isEmpty()) return null;
        MusicDirectoryQuery query = queryHistory.pop();
        currentQuery = query;
        return query;
    }

    public static boolean isEmpty() {
        return queryHistory.isEmpty();
    }
}
