package com.example.ian.supermix;

import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by ian on 10/12/2017.
 */

public class MediaBrowserClient {
    private final String TAG = "MEDIA_BROWSER_CLIENT";

    public static abstract class MediaBrowserChangedListener {
        public void onConnected(MediaControllerCompat mediaController) {}
        public void onMetadataChanged(MediaMetadataCompat mediaMetadata) {}
        public void onPlaybackStateChanged(PlaybackStateCompat playbackState) {}
        public void onRepeatModeChanged(int repeatMode) {}
        public void onShuffleModeChanged(boolean enabled) {}
    }

    private Context context;
    private final ArrayList<MediaBrowserChangedListener> listeners = new ArrayList<>();

    private MediaBrowserCompat mediaBrowser;
    private MediaControllerCompat mediaController;

    private final InternalState state;

    private final MediaBrowserConnectionCallback mediaBrowserConnectionCallback = new MediaBrowserConnectionCallback();
    private final MediaControllerCallback mediaControllerCallback = new MediaControllerCallback();

    public MediaBrowserClient(Context context) {
        this.context = context;
        state = new InternalState();
    }

    public void onStart() {
        if (mediaBrowser == null) {
            mediaBrowser = new MediaBrowserCompat(context,
                    new ComponentName(context, MusicService.class),
                    mediaBrowserConnectionCallback, null);
            mediaBrowser.connect();
        }
    }

    public void onStop() {
        if (mediaController != null) {
            mediaController.unregisterCallback(mediaControllerCallback);
            mediaController = null;
        }
        if (mediaBrowser != null && mediaBrowser.isConnected()) {
            mediaBrowser.disconnect();
            mediaBrowser = null;
        }
    }

    public MediaControllerCompat.TransportControls getTransportControls() {
        if (mediaController == null) {
            Log.e(TAG, "MediaController null in getTransportControls");
            return null;
        }
        return mediaController.getTransportControls();
    }

    public void addListener(MediaBrowserChangedListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(MediaBrowserChangedListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    public class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        @Override
        public void onConnected() {
            try {
                mediaController = new MediaControllerCompat(context, mediaBrowser.getSessionToken());
                mediaController.registerCallback(mediaControllerCallback);
                //Sync with MediaSession
                Log.v("SESSION", "BROWSER CLIENT controller repeat mode " + Integer.toString(mediaController.getRepeatMode()));
                mediaControllerCallback.onMetadataChanged(mediaController.getMetadata());
                mediaControllerCallback.onPlaybackStateChanged(mediaController.getPlaybackState());

                for(int i = 0; i < listeners.size(); i++) {
                    listeners.get(i).onConnected(mediaController);
                }
            } catch(RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public class MediaControllerCallback extends MediaControllerCompat.Callback {

        @Override
        public void onSessionDestroyed() {
            Log.e(TAG, "onSessionDestroyed: Music service died");
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            for(int i = 0; i < listeners.size(); i++) {
                listeners.get(i).onPlaybackStateChanged(state);
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            for(int i = 0; i < listeners.size(); i++) {
                listeners.get(i).onMetadataChanged(metadata);
            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            state.setRepeatMode(repeatMode);
            for(int i = 0; i < listeners.size(); i++) {
                listeners.get(i).onRepeatModeChanged(state.getRepeatMode());
            }
        }

        @Override
        public void onShuffleModeChanged(boolean enabled) {
            Log.v("SESSION", "media browser client shuffle where are listeners");
            for(int i = 0; i < listeners.size(); i++) {
                Log.v("SESSION", "media browser client on shuffled enabled");
                listeners.get(i).onShuffleModeChanged(enabled);
            }
        }
    }

    public class InternalState {
        public int getRepeatMode() {
            return repeatMode;
        }

        public void setRepeatMode(int repeatMode) {
            this.repeatMode = repeatMode;
        }

        public void reset() {
            repeatMode = PlaybackStateCompat.REPEAT_MODE_NONE;
        }

        private int repeatMode;
    }
}
