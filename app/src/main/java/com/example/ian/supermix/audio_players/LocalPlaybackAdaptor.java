package com.example.ian.supermix.audio_players;

import android.content.ContentUris;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.io.IOException;

/**
 * Created by ian on 10/16/2017.
 */

public class LocalPlaybackAdaptor extends PlaybackAdaptor {
    private static final int PREVIOUS_DISABLED_BUFFER = 3000;
    private final Context context;
    private MediaPlayer mediaPlayer;
    private long audioId = -1;
    private PlaybackChangeListener playbackChangeListener;
    private MediaMetadataCompat currentAudio;
    private int state;
    private boolean currentAudioPlayedToCompletion;

    //TODO find out more on MediaPlayer seekTo bug
    private int seekWhileNotPlaying = -1;

    public LocalPlaybackAdaptor(Context context, PlaybackChangeListener listener) {
        super(context);
        this.context = context.getApplicationContext();
        playbackChangeListener = listener;
    }

    private void initMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playbackChangeListener.onPlaybackCompleted();
                    // setNewState(PlaybackStateCompat.STATE_PAUSED);
                }
            });
        }
    }

    @Override
    public void playFromMetadata(MediaMetadataCompat metadata) {
        currentAudio = metadata;
        Log.v("PLAYER", metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
        final long audioId = Long.parseLong(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
        playAudio(audioId);
    }

    @Override
    public MediaMetadataCompat getCurrentMetadata() {
        return currentAudio;
    }

    private void playAudio(long audioId) {
        boolean audioChanged = (audioId == -1 || audioId != this.audioId);
        if(currentAudioPlayedToCompletion) {
            audioChanged = true;
            currentAudioPlayedToCompletion = false;
        }
        if (!audioChanged) {
            // seekTo(0);
            if (!isPlaying()) {
                play();
            }
            return;
        } else {
            release();
        }

        this.audioId = audioId;
        initMediaPlayer();

        Uri audioUri =
            ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                this.audioId);

        try {
            mediaPlayer.setDataSource(context, audioUri);
        } catch (IOException e) {
            throw new RuntimeException("Failed to open file");
        }

        try {
            mediaPlayer.prepare();
        } catch (Exception e) {
            throw new RuntimeException("Failed to open File");
        }

        play();
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    @Override
    protected void onPlay() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            Log.v("PLAYER", "starting music");
            setNewState(PlaybackStateCompat.STATE_PLAYING);
        }
    }

    @Override
    public boolean canGoToPrevious () {
        return mediaPlayer.getCurrentPosition() < PREVIOUS_DISABLED_BUFFER;
    }

    @Override
    protected void onPause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            setNewState(PlaybackStateCompat.STATE_PAUSED);
        }
    }

    @Override
    protected void onStop() {
        setNewState(PlaybackStateCompat.STATE_STOPPED);
        release();
    }

    private void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void seekTo(long position) {
        if (mediaPlayer != null) {
            if (!mediaPlayer.isPlaying()) {
                seekWhileNotPlaying = (int) position;
            }
            mediaPlayer.seekTo((int) position);
            setNewState(state);
        }
    }

    @Override
    public void setVolume(float volume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume, volume);
        }
    }

    private void setNewState(int playerState) {
        state = playerState;

        if (state == PlaybackStateCompat.STATE_STOPPED) {
            currentAudioPlayedToCompletion = true;
        }


        //TODO look up work around for when getCurrentPosition changes while not playing
        final long reportPosition;
        if (seekWhileNotPlaying >= 0) {
            reportPosition = seekWhileNotPlaying;

            if (state == PlaybackStateCompat.STATE_PLAYING) {
                seekWhileNotPlaying = -1;
            }
        } else {
            reportPosition = mediaPlayer == null ? 0 : mediaPlayer.getCurrentPosition();
        }

        final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
        stateBuilder.setActions(getAvailableActions());
        stateBuilder.setState(state, reportPosition, 1.0f, SystemClock.elapsedRealtime());
        playbackChangeListener.onPlaybackStateChange(stateBuilder.build());
    }

    private long getAvailableActions() {
        long actions = PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                | PlaybackStateCompat.ACTION_SET_REPEAT_MODE
                | PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE_ENABLED;

        switch (state) {
            case PlaybackStateCompat.STATE_STOPPED:
                actions |= PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PAUSE;
                break;
            case PlaybackStateCompat.STATE_PLAYING:
                actions |= PlaybackStateCompat.ACTION_STOP
                        | PlaybackStateCompat.ACTION_PAUSE
                        | PlaybackStateCompat.ACTION_SEEK_TO;
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                actions |= PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_STOP;
                break;
            default:
                actions |= PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PLAY_PAUSE
                        | PlaybackStateCompat.ACTION_STOP
                        | PlaybackStateCompat.ACTION_PAUSE;
                break;
        }
        return actions;
    }
//    public void initPlayer() {
//        player.setWakeMode(context,
//                PowerManager.PARTIAL_WAKE_LOCK);
//        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
//        player.setOnPreparedListener(context);
//        player.setOnCompletionListener(this);
//        player.setOnErrorListener(this);
//    }
}
