package com.example.ian.deffsound;

import android.util.Log;

import com.example.ian.deffsound.songview.Song;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Ian on 11/25/2016.
 */
public class SongQueue {
    private int curPosInQueue;
    private boolean shuffleOn;
    private boolean repeatQueueOn;
    private boolean repeatSongOn;
    private ArrayList<Song> songList;
    private int shuffleMapping[];

    //shuffle sort for everytime shuffle is pressed

    public SongQueue(int curPosInQueue, ArrayList<Song> songList) {
        this.curPosInQueue = curPosInQueue;
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
            if(curPosInQueue + 1 >= songList.size()) curPosInQueue = 0;
            else curPosInQueue += 1;
        } else {
            Log.e("NEXT", "Current Song index: " + curPosInQueue);
            //fringe cases
            if(curPosInQueue + 1 >= songList.size()) return null;
            curPosInQueue += 1;
        }
        //check if queue is shuffled
        if(shuffleOn) {
            Log.e("SHUFFLE", "Current Song: " + curPosInQueue + "Next Shuffled Song: " + shuffleMapping[curPosInQueue]);
            return songList.get(shuffleMapping[curPosInQueue]);
        }
        return getCurrentSong();
    }

    public Song prevSong() {
        //check if repeatSong is set
        if(repeatSongOn) {
            //if repeat song, do nothing
            //check if queue is shuffled
        } else if(repeatQueueOn) {
            //check if repeatQueue is set
            if(curPosInQueue - 1 < 0) curPosInQueue = songList.size()-1;
            else curPosInQueue -= 1;
        } else {
            if(curPosInQueue - 1 < 0) return null;
            curPosInQueue -= 1;
        }
        //check if queue is shuffled
        if(shuffleOn) return songList.get(shuffleMapping[curPosInQueue]);
        return getCurrentSong();
    }

    public Song getCurrentSong() {
        Log.e("CURSONG", "index now: " + curPosInQueue);
        if(shuffleOn) return songList.get(shuffleMapping[curPosInQueue]);
        return songList.get(curPosInQueue);
    }

    public void setCurrentSong(int i) {
        //if curPosInQueue changed manually, shuffled order needs to be reset
        curPosInQueue = i;
       // if(shuffleOn) shuffleQueue();
    }

    public Song getSong(int i) {
        return songList.get(i);
    }

    public ArrayList<Song> getSongList() {
        return songList;
    }

    private int[] shuffleQueue() {
        int[] shuffled = new int[songList.size()];
        for(int i = 0; i < shuffled.length; i++) {
            shuffled[i] = i;
        }
        for(int i = 0; i < shuffled.length; i++) {
            if(i == curPosInQueue) continue;
            int rand = (int) (Math.random() * (songList.size() - i) + i);
            while(rand == curPosInQueue)
                rand = (int) (Math.random() * (songList.size() - i) + i);
            int swap = shuffled[i];
            shuffled[i] = shuffled[rand];
            shuffled[rand] = swap;
        }

        Log.e("SHUFFLE", Arrays.toString(shuffled));
        int [] a = Arrays.copyOf(shuffled, songList.size());
        Arrays.sort(a);
        Log.e("SHUFFLE", Arrays.toString(a));

        return shuffled;
    }

    public void resetQueue() {
        curPosInQueue = 0;
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
            curPosInQueue = shuffleMapping[curPosInQueue];
        } else {
            shuffleOn = true;
            shuffleMapping = shuffleQueue();
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
