package com.example.ian.supermix.audio_players;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.support.v4.media.MediaMetadataCompat;

/**
 * Created by ian on 10/8/2017.
 */

public abstract class PlaybackAdaptor {

    private static final float VOLUME_DEFAULT = 1.0f;
    private static final float VOLUME_DUCK = 0.2f;

    private static final IntentFilter AUDIO_NOISY_FILTER =
            new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

    private boolean noisyAudioReceiverRegistered = false;
    private BroadcastReceiver noisyAudioReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                if (isPlaying()) {
                    pause();
                }
            }
        }
    };

    private final Context appContext;
    private final AudioManager audioManager;
    private AudioFocusHelper audioFocusHelper;

    private boolean playOnAudioFocus = false;

    public PlaybackAdaptor(Context context) {
        appContext = context.getApplicationContext();
        audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
        audioFocusHelper = new AudioFocusHelper();
    }

    public abstract void playFromMetadata(MediaMetadataCompat metadata);

    public abstract MediaMetadataCompat getCurrentMetadata();

    public abstract boolean isPlaying();

    public final void play() {
        if(audioFocusHelper.requestAudioFocus()) {
            registerNoisyAudioReceiver();
            onPlay();
        }
    }

    protected abstract void onPlay();
    public abstract boolean canGoToPrevious();

    public final void pause() {
        if (!playOnAudioFocus) {
            audioFocusHelper.abandonAudioFocus();
        }

        unregisterNoisyAudioReceiver();
        onPause();
    }

    protected abstract void onPause();

    public final void stop() {
        audioFocusHelper.abandonAudioFocus();
        onStop();
    }

    protected abstract void onStop();

    public abstract void seekTo(long position);

    public abstract void setVolume(float volume);

    private void registerNoisyAudioReceiver() {
        if (!noisyAudioReceiverRegistered) {
            appContext.registerReceiver(noisyAudioReceiver, AUDIO_NOISY_FILTER);
            noisyAudioReceiverRegistered = true;
        }
    }

    private void unregisterNoisyAudioReceiver() {
        if (noisyAudioReceiverRegistered) {
            appContext.unregisterReceiver(noisyAudioReceiver);
            noisyAudioReceiverRegistered = false;
        }
    }

//    private MediaStyleNotification playerNotification;
//    private MediaSessionCompat mediaSession;
//    private MusicService musicService;
//    private MediaPlayer player;
//
//    MediaSessionCompat.Callback callback = new MediaSessionCompat.Callback() {
//        @Override
//        public void onPlay() {
//            //super.onPlay();
//            AudioManager audioManager =
//        }
//
//        @Override
//        public void onPause() {
//            super.onPause();
//        }
//
//        @Override
//        public void onStop() {
//            super.onStop();
//        }
//    };

    private final class AudioFocusHelper implements AudioManager.OnAudioFocusChangeListener {
        private boolean requestAudioFocus() {
            int result = audioManager.requestAudioFocus(this,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }

        private void abandonAudioFocus() {
            audioManager.abandonAudioFocus(this);
        }

        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (playOnAudioFocus && !isPlaying()) {
                        play();
                    } else if(isPlaying()) {
                        setVolume(VOLUME_DEFAULT);
                    }
                    playOnAudioFocus = false;
                    break;
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                    setVolume(VOLUME_DUCK);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if(isPlaying()) {
                        playOnAudioFocus = true;
                        pause();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    abandonAudioFocus();
                    playOnAudioFocus = false;
                    stop();
                    break;
            }
        }
    }
}
