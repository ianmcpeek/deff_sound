package com.example.ian.deffsound.songqueue;

import android.util.Log;

import com.example.ian.deffsound.songview.Song;

import java.util.ArrayList;

/**
 * Created by ian on 7/26/2017.
 */

public class NowPlayingController {
    private SongPlaylist playlist;
    private SongPlaylist shuffledPlaylist;

    private boolean isShuffled;
    private RepeatState repeatState;

    public NowPlayingController() {
        this.playlist = new SongPlaylist();
        this.shuffledPlaylist = new SongPlaylist();
        this.isShuffled = false;
        this.repeatState = RepeatState.OFF;
    }

    public boolean isShuffled() {
        return isShuffled;
    }

    public void toggleShuffle() {
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

    public RepeatState getRepeatState() {
        return repeatState;
    }

    public void toggleRepeatState() {
        switch (repeatState) {
            case OFF:
                repeatState = RepeatState.PLAYLIST;
                break;
            case PLAYLIST:
                repeatState = RepeatState.SONG;
                break;
            case SONG:
                repeatState = RepeatState.OFF;
                break;
        }
    }

    public Song getPreviousAvailableSong() {
        switch (repeatState) {
            case OFF:
                return getActivePlaylist().previousSong();

            case PLAYLIST:
                Song prevSong = getActivePlaylist().previousSong();
                if(prevSong == null) {
                    setActivePlaylistCurrentSong(getActivePlaylist().size() - 1);
                    prevSong = getAvailableSong();
                }
                return prevSong;

            case SONG:
                return getAvailableSong();
        }
        return null;
    }

    public Song getNextAvailableSong() {
        switch (repeatState) {
            case OFF:
                return getActivePlaylist().nextSong();

            case PLAYLIST:
                Song nextSong = getActivePlaylist().nextSong();
                if(nextSong == null) {
                    setActivePlaylistCurrentSong(0);
                    nextSong = getAvailableSong();
                }
                return nextSong;

            case SONG:
                return getAvailableSong();
        }
        return null;
    }

    public Song getAvailableSong() {
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
        this.playlist = new SongPlaylist(playlist);
        this.playlist.setCurrentSong(position);
        if (isShuffled) {
            setShuffledPlaylist();
        }

    }

    public boolean isActivePlaylistSet() {
        return !getActivePlaylist().isEmpty();
    }
}
