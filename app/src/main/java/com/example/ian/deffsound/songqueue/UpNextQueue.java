package com.example.ian.deffsound.songqueue;

import android.util.Log;

import com.example.ian.deffsound.songview.Song;

import java.util.LinkedList;

/**
 * Created by ian on 7/29/2017.
 */

public class UpNextQueue {
    private LinkedList<Song> queue;

    public UpNextQueue() {
        this.queue = new LinkedList<>();
    }

    public void enqueue(Song song) {
        String queueContents = "[";
        for(int i = 0; i < queue.size(); i++) {
            queueContents += queue.get(i).getTitle() + ", ";
        }
        queueContents += "]";
        Log.v("SONG PICKED", queueContents);
        queue.add(song);
    }

    public Song dequeue() {
        return queue.remove();
    }

    public Song peek() {
        return queue.peek();
    }
}
