package com.example.ian.supermix;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

/**
 * Created by ian on 10/22/2017.
 */

public class AudioNotificationManager {

    public static final int NOTIFICATION_ID = 412;

    private static final String TAG = "AudioNotification";
    private static final int REQUEST_CODE = 501;

    private final MusicService musicService;

    private final NotificationCompat.Action playAction;
    private final NotificationCompat.Action pauseAction;
    private final NotificationCompat.Action nextAction;
    private final NotificationCompat.Action prevAction;
    private  final NotificationManager notificationManager;

    public AudioNotificationManager(MusicService service) {
        musicService = service;

        notificationManager = (NotificationManager) musicService.getSystemService(Context.NOTIFICATION_SERVICE);

        playAction = new NotificationCompat.Action(R.drawable.ic_play_arrow_accent_24dp,
                musicService.getString(R.string.label_play),
                MediaButtonReceiver.buildMediaButtonPendingIntent(musicService,
                        PlaybackStateCompat.ACTION_PLAY));

        pauseAction = new NotificationCompat.Action(R.drawable.ic_pause_accent_24dp,
                musicService.getString(R.string.label_pause),
                MediaButtonReceiver.buildMediaButtonPendingIntent(musicService,
                        PlaybackStateCompat.ACTION_PAUSE));

        nextAction = new NotificationCompat.Action(R.drawable.ic_skip_next_accent_24dp,
                musicService.getString(R.string.label_next),
                MediaButtonReceiver.buildMediaButtonPendingIntent(musicService,
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT));

        prevAction = new NotificationCompat.Action(R.drawable.ic_skip_previous_accent_24dp,
                musicService.getString(R.string.label_prev),
                MediaButtonReceiver.buildMediaButtonPendingIntent(musicService,
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));

        notificationManager.cancelAll();
    }

    public void onDestroy() {
        Log.d(TAG, "onDestroy");
    }

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public Notification getNotification(MediaMetadataCompat metadata,
                                        PlaybackStateCompat state,
                                        MediaSessionCompat.Token token) {
        boolean isPlaying = state.getState() == PlaybackStateCompat.STATE_PLAYING;
        MediaDescriptionCompat description = metadata.getDescription();
        Bitmap albumArt = metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART);
        NotificationCompat.Builder builder = buildNotification(state, token, isPlaying, description, albumArt);
        return builder.build();
    }

    private NotificationCompat.Builder buildNotification(PlaybackStateCompat state,
                                                         MediaSessionCompat.Token token,
                                                         boolean isPlaying,
                                                         MediaDescriptionCompat description,
                                                         Bitmap albumArt) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(musicService);
        builder.setStyle(new android.support.v7.app.NotificationCompat.MediaStyle()
                        .setMediaSession(token)
                .setShowActionsInCompactView(0, 1, 2)
                .setShowCancelButton(true)
                .setCancelButtonIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(musicService, PlaybackStateCompat.ACTION_STOP)))
                .setColor(ContextCompat.getColor(musicService, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_audiotrack_accent_24dp)
                .setContentIntent(createContentIntent())
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setLargeIcon(albumArt)
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(musicService, PlaybackStateCompat.ACTION_STOP))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        if ((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0) {
            builder.addAction(prevAction);
        }
        builder.addAction(isPlaying ? pauseAction : playAction);
        if ((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0) {
            builder.addAction(nextAction);
        }
        return builder;
    }

    private PendingIntent createContentIntent() {
        Intent openUi = new Intent(musicService, NowPlayingActivity.class);
        openUi.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(musicService, REQUEST_CODE, openUi, PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
