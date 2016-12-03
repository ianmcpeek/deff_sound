package com.example.ian.deffsound;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Ian on 11/25/2016.
 */
public class SongQueue {
    private int currentSong;
    private boolean shuffleOn;
    private boolean repeatQueueOn;
    private boolean repeatSongOn;
    private ArrayList<Song> songList;
    private int shuffleMapping[];

    //shuffle sort for everytime shuffle is pressed

    public SongQueue(int currentSong, ArrayList<Song> songList) {
        this.currentSong = currentSong;
        this.songList = songList;

        //check userPreferences for current control preferences (repeat, shuffle, etc.)
        this.shuffleOn = false;
        this.repeatQueueOn = false;
        this.repeatSongOn = false;
    }

    public Song nextSong() {
        Log.e("NEXT", "NEXT SONG CALLED");
        //check if repeatSong is set
        if(repeatSongOn) {
            //if repeat song, do nothing
            //check if queue is shuffled
            Log.e("NEXT", "SONG ON REPEAT");
        } else if(repeatQueueOn) {
            Log.e("NEXT", "QUEUE ON REPEAT");
            //check if repeatQueue is set
            if(currentSong + 1 >= songList.size()) currentSong = 0;
            else currentSong += 1;
        } else {
            Log.e("NEXT", "Current Song index: " + currentSong);
            //fringe cases
            if(currentSong + 1 >= songList.size()) return null;
            currentSong += 1;
        }
        //check if queue is shuffled
        if(shuffleOn) return songList.get(shuffleMapping[currentSong]);
        return getCurrentSong();
    }

    public Song prevSong() {
        //check if repeatSong is set
        if(repeatSongOn) {
            //if repeat song, do nothing
            //check if queue is shuffled
        } else if(repeatQueueOn) {
            //check if repeatQueue is set
            if(currentSong - 1 < 0) currentSong = songList.size()-1;
            else currentSong -= 1;
        } else {
            if(currentSong - 1 < 0) return null;
            currentSong -= 1;
        }
        //check if queue is shuffled
        if(shuffleOn) return songList.get(shuffleMapping[currentSong]);
        return getCurrentSong();
    }

    public Song getCurrentSong() {
        Log.e("CURSONG", "index now: " + currentSong);
        return songList.get(currentSong);
    }

    public void setCurrentSong(int i) {
        //if currentSong changed manually, shuffled order needs to be reset
        currentSong = i;
        if(shuffleOn) shuffleQueue();
    }

    public Song getSong(int i) {
        return songList.get(i);
    }

    public int getCurrentSongIndex() {
        return currentSong;
    }

    public ArrayList<Song> getSongList() {
        return songList;
    }

    public void shuffleQueue() {
        shuffleMapping = new int[songList.size()];
        int index = 0;
        for(int i = 0; i < shuffleMapping.length; i++) {
            shuffleMapping[i] = i;
        }
        //broken, allows shuffled songs to swap with themselves
        for(int i = 0; i < shuffleMapping.length; i++) {
            int rand = (int) (Math.random() * (songList.size() - i) + i);
            int swap = shuffleMapping[i];
            shuffleMapping[i] = shuffleMapping[rand];
            shuffleMapping[rand] = swap;
        }
    }

    public boolean isShuffled() {
        return shuffleOn;
    }

    public boolean isRepeatSong() {
        return repeatSongOn;
    }

    public boolean isRepeatQueue() {
        return repeatQueueOn;
    }

    public void toggleShuffle() {
        if(shuffleOn) {
            shuffleOn = false;
        } else {
            shuffleOn = true;
            shuffleQueue();
        }
    }

    public void toggleRepeat() {
        if(!repeatQueueOn && !repeatSongOn) {
            repeatQueueOn = true;
        } else if(!repeatSongOn) {
            repeatQueueOn = false;
            repeatSongOn = true;
        } else {
            repeatSongOn = false;
        }
    }
}