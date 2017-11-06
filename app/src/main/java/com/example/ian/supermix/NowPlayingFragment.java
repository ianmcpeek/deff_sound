package com.example.ian.supermix;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class NowPlayingFragment extends Fragment {

    private MediaBrowserClient mediaBrowserClient;
    private boolean isPlaying = false;

    private TextView songText;
    private TextView artistText;
    private ImageView expandBtn;
    private ImageView playBtn;

    public NowPlayingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaBrowserClient = new MediaBrowserClient(getActivity().getApplicationContext());
        mediaBrowserClient.addListener(new MediaBrowserListener());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_now_playing, container, false);

        songText = (TextView) v.findViewById(R.id.fragSongTxt);
        artistText = (TextView) v.findViewById(R.id.fragArtistTxt);

        expandBtn = (ImageView) v.findViewById(R.id.fragmentUpBtn);
        expandBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nowPlayingIntent = new Intent(getActivity(), NowPlayingActivity.class);
                startActivity(nowPlayingIntent);
            }
        });
        playBtn = (ImageView) v.findViewById(R.id.fragmentPlayBtn);
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
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        mediaBrowserClient.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mediaBrowserClient.onStop();
    }

    private class MediaBrowserListener extends MediaBrowserClient.MediaBrowserChangedListener {
        @Override
        public void onConnected(MediaControllerCompat mediaController) {
            super.onConnected(mediaController);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat mediaMetadata) {
            if (mediaMetadata == null) {
                return;
            }
            songText.setText(mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
            artistText.setText(mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));

            if(songText.getText().length() > artistText.getText().length()) {
                songText.setSelected(true);
            } else {
                artistText.setSelected(true);
            }
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat playbackState) {
            isPlaying = playbackState != null &&
                    playbackState.getState() == PlaybackStateCompat.STATE_PLAYING;
            if (isPlaying) {
                playBtn.setImageResource(R.drawable.ic_pause_accent_24dp);
            } else {
                playBtn.setImageResource(R.drawable.ic_play_arrow_accent_24dp);
            }

        }
    }
}
