package com.example.ian.supermix;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntegerRes;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.SeekBar;

/**
 * Created by ian on 10/18/2017.
 */

public class AudioSeekBar extends AppCompatSeekBar {
    private MediaControllerCompat mediaController;
    private ControllerCallback controllerCallback;

    private boolean isTracking = false;

    private OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isTracking = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mediaController.getTransportControls().seekTo(getProgress());
            isTracking = false;
        }
    };
    private ValueAnimator progressAnimator;

    public AudioSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setOnSeekBarChangeListener(seekBarChangeListener);
    }

    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        // don't add seek listeners to this subclass
        // TODO find out why
    }

    public void setMediaController(final MediaControllerCompat mediaController) {
        if (mediaController != null) {
            controllerCallback = new ControllerCallback();
            mediaController.registerCallback(controllerCallback);
            //sync with mediaController
            controllerCallback.onMetadataChanged(mediaController.getMetadata());
            controllerCallback.onPlaybackStateChanged(mediaController.getPlaybackState());
        } else if (this.mediaController != null) {
            this.mediaController.unregisterCallback(controllerCallback);
            controllerCallback = null;
        }
        this.mediaController = mediaController;
    }

    public void disconnectController() {
        if (mediaController != null) {
            mediaController.unregisterCallback(controllerCallback);
            controllerCallback = null;
            mediaController = null;
        }
    }

    private class ControllerCallback extends  MediaControllerCompat.Callback
        implements ValueAnimator.AnimatorUpdateListener {

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            Log.v("SEEKBAR", "Playback state changed ");
            if (progressAnimator != null) {
                progressAnimator.cancel();
                progressAnimator = null;
            }

            final int progress =
                    state != null ? (int) state.getPosition() : 0;
            setProgress(progress);

            if (state != null && state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                final int timeToEnd = (int) ((getMax() - progress) / state.getPlaybackSpeed());
                if (timeToEnd < 0) {
                    return;
                }
                //TODO find out what this is
                progressAnimator = ValueAnimator.ofInt(progress, getMax()).setDuration(timeToEnd);
                progressAnimator.setInterpolator(new LinearInterpolator());
                progressAnimator.addUpdateListener(this);
                progressAnimator.start();
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            final int max =
                    metadata != null ? (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION) : 0;
            Log.v("SEEKBAR", "METADATA changed, max is now " + Integer.toString(max));
            setProgress(0);
            setMax(max);
        }

        @Override
        public void onAnimationUpdate(final ValueAnimator valueAnimator) {
            if(isTracking) {
                valueAnimator.cancel();
                return;
            }

            final int animatedIntValue = (int) valueAnimator.getAnimatedValue();
            setProgress(animatedIntValue);
        }
    }
}
