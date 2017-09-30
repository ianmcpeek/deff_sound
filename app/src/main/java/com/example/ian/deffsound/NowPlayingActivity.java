package com.example.ian.deffsound;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.ian.deffsound.songview.Song;


public class NowPlayingActivity extends AppCompatActivity {
    private MusicService songPlayerService;
    private MusicServiceReceiver serviceReceiver;
    private Handler songProgressHandler;
    private Runnable songProgressRunnable;
    private boolean musicBound = false;

    //widget controls
    private SeekBar mSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);
        serviceReceiver = new MusicServiceReceiver();

        ImageView downBtn = (ImageView) findViewById(R.id.downBtn);
        downBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // ------- Grab Seekbar -------
        mSeekBar = (SeekBar) this.findViewById(R.id.seekBar);
        //make sure connection is established before wiring up seekbar
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // for skipping through the song when holding skip bar
                if (fromUser) songPlayerService.seekTo(progress);
                updateSongProgressSeekBar();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                songProgressHandler.removeCallbacks(songProgressRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                songProgressHandler.postDelayed(songProgressRunnable, 0);
            }
        });


        // -------- Create Handler For Updating Song Progress -------
        songProgressHandler = new Handler();
        songProgressRunnable = new Runnable() {
            @Override
            public void run() {
                updateSongProgressSeekBar();
                songProgressHandler.postDelayed(this, 1000);
            }
        };
    }

    private void updateSongProgressSeekBar() {
        int pos = songPlayerService.getPosition();
        TextView songProgressView = (TextView) findViewById(R.id.trackProgressTxt);
        songProgressView.setText(timeFormat(pos));
        mSeekBar.setProgress(pos);
    }

    @Override
    protected void onDestroy() {
        //stopService(playIntent);
        //songPlayerService = null;
        if(musicBound)
            unbindService(musicConnection);
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter =
                new IntentFilter("SONG_PREPARED");
        LocalBroadcastManager.getInstance(this).registerReceiver(serviceReceiver, filter);
        if(musicBound) {
            setPlayerControls();
            setScreen();
            updateSongProgressSeekBar();
            songProgressHandler.postDelayed(songProgressRunnable, 0);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceReceiver);
        songProgressHandler.removeCallbacks(songProgressRunnable);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!musicBound) {
            Intent playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, 0);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
//        if(musicBound)
//            unbindService(musicConnection);
    }

    private ServiceConnection musicConnection =
            new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
                    songPlayerService = binder.getService();
                    musicBound = true;
                    //not sure what this is doing
                    serviceReceiver.onReceive(null, null);
                    setPlayerControls();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    musicBound = false;
                }
            };


    //SONG PLAYER CONTROLS
    public void playSong(View view) {
        if(songPlayerService == null) {
            Log.e("MUSIC SERVICE", "music service not set");
            return;
        }

        if(songPlayerService.isPlaying()) {
            songPlayerService.pausePlayer();
        } else {
            songPlayerService.play();
        }
        setPlayButton();
    }

    private void setPlayButton() {
        ImageView playBtn = (ImageView)findViewById(R.id.playBtn);
        if(!songPlayerService.isPlaying()) {
            playBtn.setImageResource(R.drawable.ic_play_arrow_accent_24dp);
        } else {
            playBtn.setImageResource(R.drawable.ic_pause_accent_24dp);
        }
    }

    public void nextSong(View view) {
        if(songPlayerService == null) {
            Log.e("MUSIC SERVICE",
                    "music service not set");
            return;
        }
        if(!songPlayerService.next()) setPlayButton();
        else setScreen();
    }

    public void prevSong(View view) {
        if(songPlayerService == null) {
            Log.e("MUSIC SERVICE",
                    "music service not set");
            return;
        }
       if( !songPlayerService.prev()) setPlayButton();
       else setScreen();
    }

    public void shuffleSongs(View view) {
        songPlayerService.toggleShuffle();
        setShuffleButton();
    }

    private void setShuffleButton() {
        ImageView shuffleBtn = (ImageView)findViewById(R.id.shuffleBtn);
        if(songPlayerService.isShuffled()) {
            shuffleBtn.setImageResource(R.drawable.ic_shuffle_accent_24dp);
        } else {
            shuffleBtn.setImageResource(R.drawable.ic_shuffle_disabled_24dp);
        }
    }

    public void repeatSongs(View view) {
       songPlayerService.toggleRepeatState();
       setRepeatStateButton();
    }

    private void setRepeatStateButton() {
        ImageView repeatBtn = (ImageView)findViewById(R.id.repeatBtn);
        switch (songPlayerService.getRepeatState()) {
            case OFF:
                repeatBtn.setImageResource(R.drawable.ic_autorenew_disabled_24dp);
                break;
            case PLAYLIST:
                repeatBtn.setImageResource(R.drawable.ic_autorenew_accent_24dp);
                break;
            case SONG:

                repeatBtn.setImageResource(R.drawable.ic_replay_accent_24dp);
                break;
        }
    }

    //VIEW UPDATES
    private void setScreen() {
        if(musicBound) {
            TextView artistView = (TextView) findViewById(R.id.artistTxt);
            TextView albumView = (TextView) findViewById(R.id.albumTxt);
            TextView songView = (TextView) findViewById(R.id.songTxt);
            artistView.setText(songPlayerService.getCurrentSong().getArtist());
            albumView.setText(songPlayerService.getCurrentSong().getAlbum());
            songView.setText(songPlayerService.getCurrentSong().getTitle());
            songView.setSelected(true);
            songView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            songView.setSingleLine(true);

        }
    }

    private void setPlayerControls() {
        if(musicBound) {
            setPlayButton();
            setShuffleButton();
            setRepeatStateButton();
        }
    }

    private String timeFormat(int milliseconds) {
        String format;
        int seconds = (milliseconds/1000) % 60;
        int minutes = (milliseconds/1000/60) % 60;
        //do time formating "00:07"
        //format += minutes < 10 ? "0" + minutes : minutes;
        format = minutes + ":";
        format += seconds < 10 ? "0" + seconds : seconds;
        //add check for hours
        return format;
    }

    private void setSongProgressSeekbar() {
        TextView songDurationView = (TextView) findViewById(R.id.trackEndTxt);
        songDurationView.setText(timeFormat(songPlayerService.getDuration()));
        mSeekBar.setMax(songPlayerService.getDuration());

        updateSongProgressSeekBar();

        songProgressHandler.postDelayed(songProgressRunnable, 0);
    }

    private void setAlbumCoverImage() {
        ContentResolver musicResolve = getContentResolver();
        Uri albumUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Song song = songPlayerService.getCurrentSong();
        Cursor music =musicResolve.query(albumUri, null,
                MediaStore.Audio.Media.TITLE + " LIKE ? AND ALBUM LIKE ?",
                new String[]{
                        song.getTitle(),
                        song.getAlbum()
                }, null);

        music.moveToFirst();
        int x=music.getColumnIndex(MediaStore.Audio.Media.DATA);
        String songPath = music.getString(x);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(songPath);


        ImageView image=(ImageView)findViewById(R.id.albumArt);
        try {
            byte[] albumArtData = retriever.getEmbeddedPicture();
            Bitmap albumArt = BitmapFactory.decodeByteArray(albumArtData, 0, albumArtData.length);
            image.setImageBitmap(albumArt);
        } catch (Exception e) {
            image.setImageResource(R.drawable.no_album_art);
        }
    }

    //Recieves notification of a prepared Song
    private class MusicServiceReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v("PREPARED", "recieved prepared");
            setScreen();
            setPlayerControls();
            setSongProgressSeekbar();
            setAlbumCoverImage();
        }
    }
}
