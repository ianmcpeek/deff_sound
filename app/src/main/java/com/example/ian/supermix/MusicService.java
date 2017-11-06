package com.example.ian.supermix;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.example.ian.supermix.audio_players.LocalPlaybackAdaptor;
import com.example.ian.supermix.audio_players.PlaybackAdaptor;
import com.example.ian.supermix.audio_players.PlaybackChangeListener;
import com.example.ian.supermix.songqueue.NowPlayingController;
import com.example.ian.supermix.songview.Song;

import java.util.List;

/**
 * Created by Ian on 8/28/2016.
 */
public class MusicService extends MediaBrowserServiceCompat {

    private static final String MEDIA_ROOT_ID = "music_service";

    private MediaSessionCompat mediaSession;
    private PlaybackAdaptor playbackPlayer;
    private AudioNotificationManager audioNotificationManager;
    private MediaSessionCallback mediaCallback;
    private boolean serviceInStartedState;

    private MediaMetadataCompat preparedAudio;

    private NowPlayingController nowPlayingController;
    private Bundle extras;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaSession = new MediaSessionCompat(this,
                MusicService.class.getSimpleName());

        nowPlayingController = NowPlayingController.getInstance();
        extras = new Bundle();
        extras.putInt("REPEAT_MODE", nowPlayingController.getRepeatMode());
        extras.putBoolean("SHUFFLE_MODE", nowPlayingController.isShuffled());
        mediaSession.setExtras(extras);

        mediaCallback = new MediaSessionCallback();
        mediaSession.setCallback(mediaCallback);
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        setSessionToken(mediaSession.getSessionToken());

        audioNotificationManager = new AudioNotificationManager(this);

        playbackPlayer = new LocalPlaybackAdaptor(this, new PlaybackPlayerListener());
    }

    @Override
    public void onDestroy() {
        audioNotificationManager.onDestroy();
        playbackPlayer.stop();
        mediaSession.release();
    }


    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot(MEDIA_ROOT_ID, null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(null);
    }

    public class MediaSessionCallback extends MediaSessionCompat.Callback {

        @Override
        public void onCommand(String command, Bundle extras, ResultReceiver cb) {
            if(command.equals("START_PLAYLIST")) {
                //setActivePlaylist here too
                preparedAudio = null;
                onPlay();
            }
        }

        @Override
        public void onPrepare() {
            Song currentSong = nowPlayingController.getAvailableSong();
            if (currentSong == null) {
                nowPlayingController.resetActivePlaylist();
                return;
            }
            //get mediaMetadataCompat from ContentProvider
            preparedAudio = LocalSongStorage.getMetadata(MusicService.this, currentSong);
            mediaSession.setMetadata(preparedAudio);

            if (!mediaSession.isActive()) {
                mediaSession.setActive(true);
            }
        }

        @Override
        public void onPlay() {
            if (!isReadyToPlay()) {
                Log.v("PLAYER", "Not ready to play");
                return;
            }

            if (preparedAudio == null) {
                onPrepare();
                if (preparedAudio == null) {
                    Song currentSong = nowPlayingController.getAvailableSong();
                    preparedAudio = LocalSongStorage.getMetadata(MusicService.this, currentSong);
                    mediaSession.setMetadata(preparedAudio);
                    onPause();
                    return;
                }
            }
            playbackPlayer.playFromMetadata(preparedAudio);
        }

        @Override
        public void onPause() {
            playbackPlayer.pause();
        }

        @Override
        public void onStop() {
            playbackPlayer.stop();
            mediaSession.setActive(false);
        }

        @Override
        public void onSkipToNext() {
            nowPlayingController.getNextAvailableSong();
            preparedAudio = null;
            onPlay();
        }

        @Override
        public void onSkipToPrevious() {
            if (playbackPlayer.canGoToPrevious()) {
                nowPlayingController.getPreviousAvailableSong();
                preparedAudio = null;
                onPlay();
            } else {
                playbackPlayer.seekTo(0);
            }
        }

        @Override
        public void onSeekTo(long pos) {
            playbackPlayer.seekTo(pos);
        }

        @Override
        public void onSetRepeatMode(int repeatMode) {
            Log.v("SESSION", "music service on repeat mode " + Integer.toString(repeatMode));
            nowPlayingController.toggleRepeatMode();
            mediaSession.setRepeatMode(nowPlayingController.getRepeatMode());

            extras.putInt("REPEAT_MODE", nowPlayingController.getRepeatMode());
            mediaSession.setExtras(extras);
        }

        @Override
        public void onSetShuffleModeEnabled(boolean enabled) {
            Log.v("SESSION", "music service on shuffled enabled");
            nowPlayingController.toggleShuffle();
            mediaSession.setShuffleModeEnabled(nowPlayingController.isShuffled());

            extras.putBoolean("SHUFFLE_MODE", nowPlayingController.isShuffled());
            mediaSession.setExtras(extras);
        }

        private boolean isReadyToPlay() {
            return nowPlayingController.isActivePlaylistSet();
        }
    }

    public class PlaybackPlayerListener extends PlaybackChangeListener {

        private final ServiceManager serviceManager;

        PlaybackPlayerListener() {
            serviceManager = new ServiceManager();
        }

        @Override
        public void onPlaybackCompleted() {
            nowPlayingController.getNextAvailableSong();
            preparedAudio = null;
            mediaCallback.onPlay();
        }

        @Override
        public void onPlaybackStateChange(PlaybackStateCompat state) {
            mediaSession.setPlaybackState(state);

            switch (state.getState()) {
                case PlaybackStateCompat.STATE_PLAYING:
                    serviceManager.moveServiceToStartedState(state);
                    break;
                case PlaybackStateCompat.STATE_PAUSED:
                    serviceManager.updateNotificationForPause(state);
                    break;
                case PlaybackStateCompat.STATE_STOPPED:
                    serviceManager.moveServiceOutOfStartedState(state);
                    break;
            }
        }

        class ServiceManager {

            private void moveServiceToStartedState(PlaybackStateCompat state) {
                Notification notification =
                        audioNotificationManager.getNotification(
                                playbackPlayer.getCurrentMetadata(),
                                state,
                                getSessionToken()
                        );
                if (!serviceInStartedState) {
                    startService(new Intent(MusicService.this, MusicService.class));
                    serviceInStartedState = true;
                }

                startForeground(AudioNotificationManager.NOTIFICATION_ID, notification);
            }

            private void updateNotificationForPause(PlaybackStateCompat state) {
                stopForeground(false);
                Notification notification =
                        audioNotificationManager.getNotification(
                                playbackPlayer.getCurrentMetadata(),
                                state,
                                getSessionToken()
                        );
                audioNotificationManager.getNotificationManager()
                        .notify(AudioNotificationManager.NOTIFICATION_ID, notification);
            }

            private void moveServiceOutOfStartedState(PlaybackStateCompat state) {
                stopForeground(true);
                stopSelf();
                serviceInStartedState = false;
            }
        }
    }
}
