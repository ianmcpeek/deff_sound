package com.example.ian.supermix.audio_players;

import android.support.v4.media.session.PlaybackStateCompat;

/**
 * Created by ian on 10/16/2017.
 */

public abstract class PlaybackChangeListener {
    public abstract void onPlaybackStateChange(PlaybackStateCompat state);

    public void onPlaybackCompleted() {

    }
}
