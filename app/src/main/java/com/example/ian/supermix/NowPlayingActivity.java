package com.example.ian.supermix;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;


public class NowPlayingActivity extends AppCompatActivity {
    private TextView artistView;
    private TextView albumView;
    private TextView songView;
    private TextView progressText;
    private TextView durationText;
    private ImageView albumImage;

    private ImageView playBtn;
    private ImageView shuffleBtn;
    private ImageView repeatBtn;

    private MediaBrowserClient mediaBrowserClient;
    private boolean isPlaying;
    private boolean isShuffled;
    private int repeat;
    private long position;

    private AudioSeekBar audioSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);
        initView();
        mediaBrowserClient = new MediaBrowserClient(this);
        mediaBrowserClient.addListener(new MediaBrowserListener());
    }

    private void initView() {
        artistView = (TextView) findViewById(R.id.artistTxt);
        albumView = (TextView) findViewById(R.id.albumTxt);
        songView = (TextView) findViewById(R.id.songTxt);
        albumImage = (ImageView)findViewById(R.id.albumArt);

        progressText = (TextView)findViewById(R.id.trackProgressTxt);
        durationText = (TextView)findViewById(R.id.trackEndTxt);

        ImageView downBtn = (ImageView) findViewById(R.id.downBtn);
        downBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        audioSeekBar = (AudioSeekBar) findViewById(R.id.seekBar);

        playBtn = (ImageView)findViewById(R.id.playBtn);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    mediaBrowserClient.getTransportControls().pause();
                } else {
                    mediaBrowserClient.getTransportControls().play();
                }
            }
        });

        shuffleBtn = (ImageView) findViewById(R.id.shuffleBtn);
        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaBrowserClient.getTransportControls().setShuffleModeEnabled(!isShuffled);
            }
        });

        repeatBtn = (ImageView)findViewById(R.id.repeatBtn);
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (repeat) {
                    case PlaybackStateCompat.REPEAT_MODE_NONE:
                        mediaBrowserClient.getTransportControls()
                                .setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL);
                        break;
                    case PlaybackStateCompat.REPEAT_MODE_ALL:
                        mediaBrowserClient.getTransportControls()
                                .setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE);
                        break;
                    case PlaybackStateCompat.REPEAT_MODE_ONE:
                        mediaBrowserClient.getTransportControls()
                                .setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE);
                        break;
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaBrowserClient.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        audioSeekBar.disconnectController();
        mediaBrowserClient.onStop();
    }

    private class MediaBrowserListener extends MediaBrowserClient.MediaBrowserChangedListener {
        @Override
        public void onConnected(MediaControllerCompat mediaController) {
            super.onConnected(mediaController);
            audioSeekBar.setMediaController(mediaController);

            Bundle extras = mediaController.getExtras();
            if (extras != null) {
                repeat = extras.getInt("REPEAT_MODE");
                setRepeatButton();
                isShuffled = extras.getBoolean("SHUFFLE_MODE");
                setShuffleButton();
            }
//            repeat = mediaController.getRepeatMode();
            Log.v("SESSION", "NOW PLAYING onConnected repeat mode is " + Integer.toString(mediaController.getRepeatMode()));
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat mediaMetadata) {
            Log.v("NOWPLAYING", "metadata null: " + Boolean.toString(mediaMetadata == null));
            if (mediaMetadata == null) {
                return;
            }
            artistView.setText(mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
            albumView.setText(mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM));

            songView.setSelected(true);
            songView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            songView.setSingleLine(true);
            songView.setText(mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));

            albumImage.setImageBitmap(mediaMetadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART));

            duration = mediaMetadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
            durationText.setText(
                    timeFormat((int)mediaMetadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION))
            );
        }

        private long duration;

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat playbackState) {
            if(playbackState != null) {
                isPlaying = playbackState.getState() == PlaybackStateCompat.STATE_PLAYING;
                setPlayButton();
                position = playbackState.getPosition();
                setProgressAnimation();
                progressText.setText(timeFormat((int)position));
            }
        }

        private ValueAnimator progressAnimator;

        private void setProgressAnimation() {
            if (progressAnimator != null) {
                progressAnimator.cancel();
                progressAnimator = null;
            }

            final int progress = (int) position;

            if (isPlaying) {
                final int timeToEnd = (int) ((duration - progress));
                if (timeToEnd < 0) {
                    return;
                }
                //TODO find out what this is
                progressAnimator = ValueAnimator.ofInt(progress, (int)duration).setDuration(timeToEnd);
                progressAnimator.setInterpolator(new LinearInterpolator());
                progressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        progressText.setText(timeFormat((int)animation.getAnimatedValue()));
                    }
                });
                progressAnimator.start();
            }
        }

        private void setPlayButton() {
            if(isPlaying) {
                playBtn.setImageResource(R.drawable.ic_pause_accent_24dp);
            } else {
                playBtn.setImageResource(R.drawable.ic_play_arrow_accent_24dp);
            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            Log.v("SESSION", "NOW PLAYING onRepeatModeChanged " + Integer.toString(repeatMode));
            repeat = repeatMode;
            setRepeatButton();
        }

        private void setRepeatButton() {
            switch (repeat) {
                case PlaybackStateCompat.REPEAT_MODE_NONE:
                    repeatBtn.setImageResource(R.drawable.ic_autorenew_disabled_24dp);
                    break;
                case PlaybackStateCompat.REPEAT_MODE_ALL:
                    repeatBtn.setImageResource(R.drawable.ic_autorenew_accent_24dp);
                    break;
                case PlaybackStateCompat.REPEAT_MODE_ONE:
                    repeatBtn.setImageResource(R.drawable.ic_replay_accent_24dp);
                    break;
            }
        }

        @Override
        public void onShuffleModeChanged(boolean enabled) {
            Log.v("SESSION", "now playing on shuffled enabled");
            isShuffled = enabled;
            setShuffleButton();
        }

        private void setShuffleButton() {
            if(isShuffled) {
                shuffleBtn.setImageResource(R.drawable.ic_shuffle_accent_24dp);
            } else {
                shuffleBtn.setImageResource(R.drawable.ic_shuffle_disabled_24dp);
            }
        }
    }

    public void nextSong(View view) {
        mediaBrowserClient.getTransportControls().skipToNext();
    }

    public void prevSong(View view) {
        mediaBrowserClient.getTransportControls().skipToPrevious();
    }

    // TODO still need to fix progress text view
    private String timeFormat(int milliseconds) {
        String format;
        long seconds = (milliseconds/1000) % 60;
        long minutes = (milliseconds/1000/60) % 60;
        //do time formating "00:07"
        format = minutes < 10 ? "0" + minutes : Long.toString(minutes);
        format += ":";
        format += seconds < 10 ? "0" + seconds : seconds;
        //add check for hours
        return format;
    }
}
