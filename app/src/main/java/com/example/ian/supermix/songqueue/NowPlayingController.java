package com.example.ian.supermix.songqueue;

import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.example.ian.supermix.songview.Song;

import java.util.ArrayList;

/**
 * Created by ian on 7/26/2017.
 */

public class NowPlayingController {
    private SongPlaylist playlist;
    private SongPlaylist shuffledPlaylist;
    private UpNextQueue upNextQueue;
    private boolean isUpNextPlaying;

    private boolean isShuffled;
    private int repeatMode;

    private NowPlayingController() {
        this.playlist = new SongPlaylist();
        this.shuffledPlaylist = new SongPlaylist();
        this.upNextQueue = new UpNextQueue();
        this.isShuffled = false;
        repeatMode = PlaybackStateCompat.REPEAT_MODE_NONE;
    }

    private static NowPlayingController nowPlayingController;

    public static NowPlayingController getInstance() {
        if (nowPlayingController == null) {
            nowPlayingController = new NowPlayingController();
        }
        return nowPlayingController;
    }

    public boolean isShuffled() {
        return isShuffled;
    }

    public void toggleShuffle() {
        Log.v("NOWPLAYING", "shuffle toggled");
        if(isShuffled) {
            isShuffled = false;
        } else {
            setShuffledPlaylist();
            isShuffled = true;
        }
    }

    private void setShuffledPlaylist() {
        ArrayList<Song> shuffledSongs = new ArrayList<>(playlist.size());
        // swap current song with first song
        shuffledSongs.add(playlist.getCurrentSong());
        for(int i = 0; i < playlist.size(); i++) {
            if (i == playlist.getCurrentSongPosition()) continue;
            int randomPosition =
                (int) (Math.random() * (shuffledSongs.size() - 1)) + 1;
            Song song = playlist.getSong(i);
            if(song != null) {
                shuffledSongs.add(randomPosition, song);
            } else {
                Log.e("SHUFFLE_PLAYLIST", "Song from playlist is null!");
            }
        }
        shuffledPlaylist = new SongPlaylist(shuffledSongs);
    }

    public int getRepeatMode() {
        return repeatMode;
    }

    public void toggleRepeatMode() {
        switch (repeatMode) {
            case PlaybackStateCompat.REPEAT_MODE_NONE:
                repeatMode = PlaybackStateCompat.REPEAT_MODE_ALL;
                break;
            case PlaybackStateCompat.REPEAT_MODE_ALL:
                repeatMode = PlaybackStateCompat.REPEAT_MODE_ONE;
                break;
            case PlaybackStateCompat.REPEAT_MODE_ONE:
                repeatMode = PlaybackStateCompat.REPEAT_MODE_NONE;
                break;
        }
    }

    public void addToUpNext(Song song) {
        this.upNextQueue.enqueue(song);
    }

    public Song getPreviousAvailableSong() {
        switch (repeatMode) {
            case PlaybackStateCompat.REPEAT_MODE_NONE:
                discardUpNext();
                return getActivePlaylist().previousSong();

            case PlaybackStateCompat.REPEAT_MODE_ALL:
                discardUpNext();
                Song prevSong = getActivePlaylist().previousSong();
                if(prevSong == null) {
                    setActivePlaylistCurrentSong(getActivePlaylist().size() - 1);
                    prevSong = getAvailableSong();
                }
                return prevSong;

            case PlaybackStateCompat.REPEAT_MODE_ONE:
                return getAvailableSong();
        }
        return null;
    }

    private void discardUpNext() {
        if (isUpNextPlaying) {
            upNextQueue.dequeue();
            isUpNextPlaying = false;
        }
    }

    public Song getNextAvailableSong() {
        Song nextSong;
        switch (repeatMode) {
            case PlaybackStateCompat.REPEAT_MODE_NONE:
                nextSong = getUpNext();
                if(nextSong != null) {
                    return nextSong;
                }
                return getActivePlaylist().nextSong();

            case PlaybackStateCompat.REPEAT_MODE_ALL:
                nextSong = getUpNext();
                if(nextSong != null) {
                    return nextSong;
                }
                nextSong = getActivePlaylist().nextSong();
                if(nextSong == null) {
                    setActivePlaylistCurrentSong(0);
                    nextSong = getAvailableSong();
                }
                return nextSong;

            case PlaybackStateCompat.REPEAT_MODE_ONE:
                return getAvailableSong();
        }
        return null;
    }

    private Song getUpNext() {
        if (isUpNextPlaying) {
            upNextQueue.dequeue();
            Song nextSong = upNextQueue.peek();
            if(nextSong != null) {
                return nextSong;
            }
            isUpNextPlaying = false;
            return getActivePlaylist().getCurrentSong();
        } else if (upNextQueue.peek() != null) {
            isUpNextPlaying = true;
            getActivePlaylist().nextSong();

            return upNextQueue.peek();
        }
        return null;
    }

    public Song getAvailableSong() {
        if (isUpNextPlaying) {
            return upNextQueue.peek();
        }
        return getActivePlaylist().getCurrentSong();
    }

    private SongPlaylist getActivePlaylist() {
        if(isShuffled) {
            return shuffledPlaylist;
        } else {
            return playlist;
        }
    }

    private void setActivePlaylistCurrentSong(int position) {
        getActivePlaylist().setCurrentSong(position);
    }

    public void resetActivePlaylist() {
        setActivePlaylistCurrentSong(0);
    }

    public void setActivePlaylist(ArrayList<Song> playlist, int position) {
        if (playlist == null) {
            Log.e("NOWPLAYINGCONTROLLER", "Tried setting active playlist with null!");
            return;
        }
        this.playlist = new SongPlaylist(playlist);
        this.playlist.setCurrentSong(position);
        if (isShuffled) {
            setShuffledPlaylist();
        }
        if (isUpNextPlaying) {
            upNextQueue.dequeue();
            isUpNextPlaying = false;
        }

    }

    public boolean isActivePlaylistSet() {
        return !getActivePlaylist().isEmpty();
    }
}
